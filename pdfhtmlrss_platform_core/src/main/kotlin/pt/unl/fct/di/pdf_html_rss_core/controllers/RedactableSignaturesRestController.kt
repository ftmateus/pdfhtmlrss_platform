package pt.unl.fct.di.pdf_html_rss_core.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pt.unl.fct.di.pdf_html_rss_core.dto.*
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.*
import java.nio.file.Paths
import javax.servlet.http.HttpServletResponse


//import kotlin.io.encoding.Base64


@RestController
@RequestMapping(value = ["/"])
class RedactableSignaturesRestController {

    @Autowired
    private lateinit var securityService: SecurityService

    val SUPPORTED_UPLOAD_MIME_TYPES = listOf(
        MediaType.APPLICATION_PDF,
        MediaType.TEXT_XML,
        MediaType.TEXT_HTML,
    );

    @Autowired
    lateinit var redactableSignaturesService: XHTMLRedactableSignatureService

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    lateinit var pdfManipulationService: PDFManipulationService;

    @Autowired
    lateinit var redactionProcessService: RedactionProcessService

    @Autowired
    lateinit var pAdESService: PAdESService;

    @Autowired
    lateinit var domService: DOMService;

    @GetMapping("/auth/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun checkAuthStatus() : String {
        val loggedInUser = securityService.getLoggedInUser()
            ?: return "{\"loggedIn\":false}";

        return "{\"loggedIn\":true,\"user\":\"${loggedInUser.username}\"}"
    }

    //TODO html file type?
    @GetMapping("/tmp/{filePath}")
    fun getTempFile(@PathVariable filePath: String): ResponseEntity<InputStreamResource> {
        val normalizedPath: String = Paths.get(filePath).normalize().toString()

        val tempFile = temporaryFilesRepository.getTempFileSecurely(
            normalizedPath
        )

        if(tempFile == null)
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val contentType = when(tempFile.extension) {
            "pdf" -> MediaType.APPLICATION_PDF
            "html" -> MediaType.TEXT_HTML
            else -> MediaType.TEXT_PLAIN
        }

        return ResponseEntity.ok()
//            .cacheControl(CacheControl.maxAge())
            .contentType(contentType)
            .body(InputStreamResource(tempFile.inputStream()));
    }


    //TODO post or get?
    @PostMapping("/verify")
    fun verifyDocument(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") type: MediaType,
    ) : Boolean {
        val type = MediaType.parseMediaType(file.contentType?: "")
        checkIfFileTypeIsSupported(type)

        val pdfDoc = file.inputStream.use {
            it.readBytes()
        }.let { PDFFileWrapper(file.name, it) }

        when(type) {
            MediaType.APPLICATION_PDF -> {
                if (pAdESService.verifyDocument(pdfDoc).not())
                    return false

                return if(pdfManipulationService.hasRedactableSignature(pdfDoc))
                    pdfManipulationService.verifyPdfFileRedactableSignature(pdfDoc)
                else
                    true
            }
            else -> TODO()
        }
    }

    @PostMapping("/sign/prepare")
    fun prepareFileForRedaction(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("redactionTask") action: RedactionProcessAction
    ) : RedactionProcess {
        val type = MediaType.parseMediaType(file.contentType?: "")

        checkIfFileTypeIsSupported(type)

        return when(type) {
            MediaType.APPLICATION_PDF -> {
                redactionProcessService.initiatePdfRedactionProcess(
                    PDFFileWrapper(file.resource),
                    action
                )
            }
            MediaType.TEXT_XML, MediaType.TEXT_HTML -> {
                redactionProcessService.initiateXHtmlRedactionProcess(
                    file.inputStream,
                    action
                )
            }
            else -> throw AssertionError();
        }
    }

    @GetMapping("/sign/{processId}")
    fun getRedactionProcess(
        @PathVariable processId: String
    ) : RedactionProcess {
        val loggedInUser = securityService.getLoggedInUser() ?:
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val process = redactionProcessService.getRedactionProcess(processId)

        if(process.userId != loggedInUser.userId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        return process;
    }

    @PostMapping("/sign/{processId}")
    fun finalizeRedactionProcess(
        @PathVariable processId : String,
        @RequestParam("elementsToRedact") elementsToRedact : List<String>,
        response : HttpServletResponse
    ) {
        if(elementsToRedact.isEmpty())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        response.outputStream.use { out ->
            val process = redactionProcessService
                .finalizeRedactionProcess(
                    processId,
                    elementsToRedact,
                    out
                )
            response.contentType = process.fileType
        }
    }

    @DeleteMapping("/sign/{processId}")
    fun cancelRedactionProcess(
        @PathVariable processId : String
    ) {
        val process = getRedactionProcess(processId)

        redactionProcessService.deleteRedactionProcess(process)
    }

    @PostMapping("/sign")
    fun signDocumentOnly(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<InputStreamResource> {

        val type = MediaType.parseMediaType(file.contentType?: "")
        checkIfFileTypeIsSupported(type)

        val docBytes : ByteArray = file.inputStream.use {
            it.readBytes()
        }

        var resource = when(type) {
            MediaType.APPLICATION_PDF -> {
                ByteArrayOutputStream().use {
                    pAdESService
                        .signDocument(
                            PDFFileWrapper(file.name, docBytes),
                            outputStream = it
                        )
                    it.toByteArray()
                }
            }
            MediaType.TEXT_XML,
            MediaType.TEXT_HTML -> {
                val domDoc = docBytes.inputStream().use {
                     domService.parseDocument(it)
                }

                val signedDoc = redactableSignaturesService.signDocument(domDoc)

                domService.convertDomDocumentToByteArray(signedDoc);
            }
            else -> throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
        }

//        if (type == MediaType.APPLICATION_PDF) {
//            resource = ByteArrayOutputStream().use {
//                pAdESService.signDocument(
//                    PDFFileWrapper(file.name + "_signed", resource),
//                    it);
//            }
//        }


        //InputStreamResource
        return ResponseEntity.ok()
            .contentType(type)
            .body(InputStreamResource(resource.inputStream()))
    }


    private fun checkIfFileTypeIsSupported(type : MediaType) {
        if(SUPPORTED_UPLOAD_MIME_TYPES.none { it == type })
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
    }

}
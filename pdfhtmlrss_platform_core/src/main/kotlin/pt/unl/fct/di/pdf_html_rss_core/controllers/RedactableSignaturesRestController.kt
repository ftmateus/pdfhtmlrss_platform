package pt.unl.fct.di.pdf_html_rss_core.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.dto.PendingRedactionTask
import pt.unl.fct.di.pdf_html_rss_core.dto.PendingRedactionTaskAction
import pt.unl.fct.di.pdf_html_rss_core.repositories.PendingRedactionTasksRepository
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.*
import java.nio.file.Paths
import javax.print.attribute.standard.Media


//import kotlin.io.encoding.Base64


@RestController
@RequestMapping(value = ["/"])
class RedactableSignaturesRestController {

    @Autowired
    private lateinit var pendingRedactionTasksRepository: PendingRedactionTasksRepository

    @Autowired
    private lateinit var fileConversionService: FileConversionService
    val SUPPORTED_UPLOAD_MIME_TYPES = listOf(
        MediaType.APPLICATION_PDF,
        MediaType.TEXT_XML,
        MediaType.TEXT_HTML,
    );

    @Autowired
    lateinit var redactableSignaturesService: XHTMLRedactableSignatureService

    @Autowired
    lateinit var temporaryFilesService: TemporaryFilesService

    @Autowired
    lateinit var pdfManipulationService: PDFManipulationService;

    @Autowired
    lateinit var pdfConversionService: FileConversionService

    @Autowired
    lateinit var domService: DOMService;

    @GetMapping(value = ["/test"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun testApi() : String {
        return "Hello World\n";
    }

    //TODO html file type?
    @GetMapping(value = ["/tmp/{filePath}"])
    fun getTempFile(@PathVariable filePath: String): ResponseEntity<InputStreamResource> {
        val normalizedPath: String = Paths.get(filePath).normalize().toString()

        val tempFile = temporaryFilesService.getTempFileSecurely(
            normalizedPath
        )

        if(tempFile == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok()
            .body(InputStreamResource(tempFile.inputStream()));
    }


    //TODO post or get?
    @PostMapping("/verify")
    fun verifyDocument(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") type: MediaType,
    ) : Boolean {
        //TODO extract function
        if(SUPPORTED_UPLOAD_MIME_TYPES.none { it == type })
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)

        val docBytes : ByteArray = file.inputStream.use {
            it.readBytes()
        }

        return false;
    }

    @PostMapping("/sign/prepare")
    fun preparePdfFileForRedaction(
        @RequestParam("file") file: MultipartFile
    ) : PendingRedactionTask {
        val type = MediaType.parseMediaType(file.contentType?: "")

        if (type != MediaType.APPLICATION_PDF)
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)

        val pdfFile = PDFFileWrapper(file.resource)

        val htmlTmpFile = temporaryFilesService.writeToTempFile { tmpFileOut ->
            val docBytes = pdfConversionService.generateHTMLFromPDF(pdfFile)
            tmpFileOut.write(docBytes)
        }

        val task = PendingRedactionTask(
            //TODO user id
            userId = "",
            temporaryHtmlFile = htmlTmpFile.name,
            fileType = MediaType.APPLICATION_PDF.toString(),
            action = PendingRedactionTaskAction.SELECT_REDACTABLE_ELEMS,
        )

        pendingRedactionTasksRepository.save(task)

        return task
    }

    @PostMapping("/sign")
    fun signDocument(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<InputStreamResource> {

        val type = MediaType.parseMediaType(file.contentType?: "")
        checkIfFileTypeIsSupported(type)

        val docBytes : ByteArray = file.inputStream.use {
            it.readBytes()
        }

        val resource = InputStreamResource(
            when(type) {
                MediaType.APPLICATION_PDF -> {
                    val signedDoc = pdfManipulationService
                        .signPdfFileRedactableSignature(PDFFileWrapper(file.name, docBytes))

                    ByteArrayInputStream(signedDoc.getData())
                }
                MediaType.TEXT_XML,
                MediaType.TEXT_HTML -> {
                    val domDoc = docBytes.inputStream().use {
                         domService.parseDocument(it)
                    }

                    val signedDoc = redactableSignaturesService.signDocument(domDoc)

                    ByteArrayInputStream(domService.convertDomDocumentToByteArray(signedDoc));
                }
                else -> throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
            }
        );

        return ResponseEntity.ok()
            .contentType(type)
            .body(resource)
    }


    private fun checkIfFileTypeIsSupported(type : MediaType) {
        if(SUPPORTED_UPLOAD_MIME_TYPES.none { it == type })
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
    }

}
package pt.unl.fct.di.pdf_html_rss_core.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Caching
import org.springframework.core.io.InputStreamResource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.dto.*
import pt.unl.fct.di.pdf_html_rss_core.repositories.RedactionProcessRepository
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.*
import java.nio.file.Paths


//import kotlin.io.encoding.Base64


@RestController
@RequestMapping(value = ["/"])
class RedactableSignaturesRestController {

    @Autowired
    private lateinit var compressionService: CompressionService

    @Autowired
    private lateinit var redactionProcessRepository: RedactionProcessRepository

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
    @GetMapping("/tmp/{filePath}")
    fun getTempFile(@PathVariable filePath: String): ResponseEntity<InputStreamResource> {
        val normalizedPath: String = Paths.get(filePath).normalize().toString()

        val tempFile = temporaryFilesService.getTempFileSecurely(
            normalizedPath
        )

        if(tempFile == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        return ResponseEntity.ok()
//            .cacheControl(CacheControl.maxAge())
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

        TODO()

        return false;
    }

    @PostMapping("/sign/prepare")
    fun prepareFileForRedaction(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("redactionTask") action: RedactionProcessAction
    ) : RedactionProcess {
        val type = MediaType.parseMediaType(file.contentType?: "")

        checkIfFileTypeIsSupported(type)

        val htmlTmpFile = when(type) {
            MediaType.APPLICATION_PDF -> {
                temporaryFilesService.writeToTempFile { tmpFileOut ->
                    val pdf = PDFFileWrapper(file.resource)
                    val docBytes = pdfConversionService.generateHTMLFromPDF(pdf)
                    tmpFileOut.write(docBytes)
                }
            }
            MediaType.TEXT_HTML, MediaType.TEXT_XML  -> {
                temporaryFilesService.writeToTempFile { tmpFileOut ->
                    file.inputStream.use {
                        it.copyTo(tmpFileOut)
                    }
                }
            }
            else -> throw AssertionError()
        }

        val pdfTmpFile = let {
            if(type == MediaType.APPLICATION_PDF) {
                temporaryFilesService.writeToTempFile { tmpFileOut ->
                    PDFFileWrapper(file.resource).getInputStream().use {
                        it.copyTo(tmpFileOut)
                    }
                }
            }
            else null;
        }

        val process = RedactionProcess(
            //TODO user id
            userId = "",
            tmpHtmlFile = htmlTmpFile.name,
            tmpPdfFile = pdfTmpFile?.name,
            fileType = type.toString(),
            action = action,
        )

        redactionProcessRepository.save(process)

        return process
    }

    fun finalizePdfRedactionProcess(
        process: RedactionProcess,
        processedSignedDoc : Document
    ) : PDFFileWrapper {
        val tmpPdfFile = temporaryFilesService.getTempFile(
            process.tmpPdfFile ?: ""
        ) ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        val pdf = PDFFileWrapper(tmpPdfFile);

        val compressedHtml = compressionService.compressGZip { gzipos ->
            domService.writeDocumentToStream(processedSignedDoc, gzipos)
        }

        val newPdfData = pdfManipulationService.addAttachmentsToPdf(
            pdf, mapOf(
                "rss.html.gz"
                        to compressedHtml.inputStream()
            )
        )

        return PDFFileWrapper(InputStreamResource(newPdfData.inputStream()))
    }


    //TODO extract logic to services
    @PostMapping("/sign/{processId}")
    fun finalizeRedactionProcess(
        @PathVariable processId : String,
        @RequestParam("elementsToRedact") elementsToRedact : List<String>
    ) : ResponseEntity<InputStreamResource> {
        if(elementsToRedact.isEmpty())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        val process = redactionProcessRepository.findById(processId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        if (process.expires <= System.currentTimeMillis()) {
            redactionProcessRepository.deleteById(processId)
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val htmlTmpFile = temporaryFilesService.getTempFile(process.tmpHtmlFile)
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        val htmlTmpDom = htmlTmpFile.inputStream().use {
            domService.parseDocument(it)
        }

        val processedDoc : Document = when(process.action) {
            RedactionProcessAction.SELECT_REDACTABLE_ELEMS ->
                redactableSignaturesService.signDocument(htmlTmpDom, elementsToRedact)
            RedactionProcessAction.REDACT ->
                redactableSignaturesService.redactDocument(htmlTmpDom, elementsToRedact)
            else -> throw AssertionError("Unknown Redaction Process Action!")
        }

        if(MediaType.parseMediaType(process.fileType) == MediaType.APPLICATION_PDF) {
            val pdfFileData = finalizePdfRedactionProcess(process, processedDoc)

            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(
                    InputStreamResource(pdfFileData.getInputStream())
                )
        }

        val htmlData = domService.convertDomDocumentToByteArray(processedDoc)

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .body(InputStreamResource(htmlData.inputStream()))
    }

    @DeleteMapping("/sign/{processId}")
    fun cancelRedactionProcess(
        @PathVariable processId : String
    ) {
        val process = redactionProcessRepository.findById(processId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        //TODO check userId

        redactionProcessRepository.deleteById(processId)
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
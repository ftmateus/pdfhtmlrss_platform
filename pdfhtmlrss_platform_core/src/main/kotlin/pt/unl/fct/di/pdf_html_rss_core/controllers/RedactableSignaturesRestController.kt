package pt.unl.fct.di.pdf_html_rss_core.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.FileConversionService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFManipulationService
import pt.unl.fct.di.pdf_html_rss_core.services.XHTMLRedactableSignatureService
import java.io.*


//import kotlin.io.encoding.Base64


@RestController
@RequestMapping(value = ["/"])
class RedactableSignaturesRestController {

    val SUPPORTED_UPLOAD_MIME_TYPES = listOf(
        MediaType.APPLICATION_PDF,
        MediaType.TEXT_XML,
        MediaType.TEXT_HTML
    );

    @Autowired
    lateinit var redactableSignaturesService: XHTMLRedactableSignatureService

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

    @PostMapping("/sign")
    fun signDocument(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") type : MediaType,
        redirectAttributes: RedirectAttributes
    ): ResponseEntity<InputStreamResource> {
//        redirectAttributes.addFlashAttribute(
//            "message",
//            "You successfully uploaded " + file.originalFilename + "!"
//        )
        //TODO extract function
        if(SUPPORTED_UPLOAD_MIME_TYPES.none { it == type })
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)

//        val docBytes : ByteArray = when (file.contentType?.let { MediaType.valueOf(it) }) {
//            MediaType.APPLICATION_PDF  -> {
//                pdfConversionService.generateHTMLFromPDFLinux(PDFFileWrapper("", file.bytes));
//            }
//            //TODO
//            MediaType.TEXT_XML,
//            MediaType.TEXT_HTML -> file.bytes
//            else -> throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
//        }

        val docBytes : ByteArray = file.inputStream.use {
            it.readBytes()
        }

//        val document = domService.parseDocument(ByteArrayInputStream(docBytes));

//        val pdf = pdfConversionService.f
//        return "redirect:/"

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
//            .headers(headers)
//            .contentLength(doc.size)
            .contentType(type)
            .body<InputStreamResource>(resource)
//        return Base64.getEncoder().encodeToString(doc);
    }


}
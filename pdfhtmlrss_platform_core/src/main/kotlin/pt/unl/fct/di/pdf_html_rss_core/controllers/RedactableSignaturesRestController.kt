package pt.unl.fct.di.pdf_html_rss_core.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.util.*

//import kotlin.io.encoding.Base64


@RestController
@RequestMapping(value = ["/"])
class RedactableSignaturesRestController {

    @Autowired
    lateinit var redactableSignaturesService: PDFConversionService

    @Autowired
    lateinit var pdfConversionService: PDFConversionService

    @GetMapping(value = ["/test"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun testApi() : String {
        return "Hello World";
    }

    @PostMapping("/upload")
    fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
//        redirectAttributes.addFlashAttribute(
//            "message",
//            "You successfully uploaded " + file.originalFilename + "!"
//        )
        val pdfBytes = pdfConversionService.generateHTMLFromPDF(file.bytes);

//        return "redirect:/"

        return Base64.getEncoder().encodeToString(pdfBytes);
    }


}
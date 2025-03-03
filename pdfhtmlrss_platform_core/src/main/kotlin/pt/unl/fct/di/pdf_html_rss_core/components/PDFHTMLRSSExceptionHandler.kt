package pt.unl.fct.di.pdf_html_rss_core.components

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException


@ControllerAdvice
class PDFHTMLRSSExceptionHandler {

    @ExceptionHandler(PDFHTMLRSSException::class)
    fun handleException(e : PDFHTMLRSSException) : ResponseEntity<String> {

        return ResponseEntity<String>("{\"message\":\"${e.message}\"}", e.httpStatusCode)
    }
}
package pt.unl.fct.di.pdf_html_rss_core.exceptions

import org.springframework.http.HttpStatus

class ForbiddenAccessException(message : String = "") : PDFHTMLRSSException(message) {
    override val httpStatusCode = HttpStatus.FORBIDDEN
}
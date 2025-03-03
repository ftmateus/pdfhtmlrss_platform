package pt.unl.fct.di.pdf_html_rss_core.exceptions

import org.springframework.http.HttpStatus

open class PDFHTMLRSSException : RuntimeException {

    constructor() : super("")

    constructor(message: String) : super(message)

    constructor(message : String, cause: Throwable) : super(message, cause)

    open val httpStatusCode = HttpStatus.BAD_REQUEST
}
package pt.unl.fct.di.pdf_html_rss_core.exceptions

class PDFHTMLRSSException : RuntimeException {

    constructor() : super("")

    constructor(message: String) : super(message)

    constructor(message : String, cause: Throwable) : super(message, cause)
}
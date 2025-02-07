package pt.unl.fct.di.pdf_html_rss_core.dto

import java.util.Date

data class SignatureVerificationReport(
    val isSigned: Boolean,
    val padesNotModified: Boolean? = false,
    val hasRSSSignature : Boolean? = false,
    val rssNotModified : Boolean? = false,
    val signatureDate : Date? = null,
    val issuedBy : String? = null,
    val padesAlgorithm : String? = null,
    val rssAlgorithm : String? = null,
)
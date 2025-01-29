package pt.unl.fct.di.pdf_html_rss_core.dto

import java.util.Date

data class SignatureVerificationReport(
    val padesNotModified: Boolean,
    val hasRSSSignature : Boolean,
    val rssNotModified : Boolean,
    val signatureDate : Date,
    val issuedBy : String,
    val padesAlgorithm : String,
    val rssAlgorithm : String?
)
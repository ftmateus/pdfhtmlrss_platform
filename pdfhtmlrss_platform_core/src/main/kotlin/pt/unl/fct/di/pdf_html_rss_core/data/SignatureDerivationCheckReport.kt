package pt.unl.fct.di.pdf_html_rss_core.data

data class SignatureDerivationCheckReport(
    val redactedDocumentReport : SignatureVerificationReport,
    val originalDocumentReport : SignatureVerificationReport,
    val isDerived : Boolean
)
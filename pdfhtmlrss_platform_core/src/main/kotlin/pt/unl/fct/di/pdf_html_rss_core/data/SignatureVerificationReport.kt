package pt.unl.fct.di.pdf_html_rss_core.data

import java.util.Date

data class SignatureVerificationReport(
    val isSigned: Boolean,
    val signatureDate : Date? = null,
    val issuedBy : String? = null,

    val hasExternalSignatures: Boolean = false,
    val hasRSSPAdESSignature: Boolean = false,
    val hasRSSXMLSignature : Boolean = false,

    val externalSignaturesViolated: Boolean = false,
    val rssPAdESViolated : Boolean = false,
    val rssXMLViolated : Boolean = false,

    val rssPAdESAlgorithm : String? = null,
    val rssXMLAlgorithm : String? = null,
) {
    fun isViolated() : Boolean = externalSignaturesViolated()
            || rssPAdESSignatureViolated()
            || rssXMLSignatureViolated()

    fun externalSignaturesViolated() : Boolean  = isSigned
            && hasExternalSignatures
            && externalSignaturesViolated;

    fun rssPAdESSignatureViolated() : Boolean = isSigned
            && hasRSSPAdESSignature
            && rssPAdESViolated;

    fun rssXMLSignatureViolated() : Boolean = isSigned
            && hasRSSXMLSignature
            && rssXMLViolated

    fun hasValidRSSSignature() : Boolean = isSigned
            && hasRSSPAdESSignature
            && hasRSSXMLSignature;
}
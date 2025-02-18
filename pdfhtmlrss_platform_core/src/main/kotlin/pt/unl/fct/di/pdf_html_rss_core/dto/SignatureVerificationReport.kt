package pt.unl.fct.di.pdf_html_rss_core.dto

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
    fun isViolated() : Boolean {
        return externalSignaturesViolated()
            || rssPAdESSignatureViolated()
            || rssXMLSignatureViolated()
    }

    fun externalSignaturesViolated() : Boolean {
        return isSigned && hasExternalSignatures && externalSignaturesViolated;
    }

    fun rssPAdESSignatureViolated() : Boolean {
        return isSigned && hasRSSPAdESSignature && rssPAdESViolated;
    }

    fun rssXMLSignatureViolated() : Boolean {
        return isSigned && hasRSSXMLSignature && rssXMLViolated;
    }

    fun hasValidRSSSignature() : Boolean {
        return isSigned && hasRSSPAdESSignature && hasRSSXMLSignature;
    }
}
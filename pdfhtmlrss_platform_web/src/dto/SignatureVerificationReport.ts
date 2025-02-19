export default interface SignatureVerificationReport {
    isSigned: Boolean,
    signatureDate : Date | undefined,
    issuedBy : String | undefined,

    hasExternalSignatures: Boolean
    hasRSSPAdESSignature: Boolean,
    hasRSSXMLSignature : Boolean,

    externalSignaturesViolated: Boolean,
    rssPAdESViolated : Boolean,
    rssXMLViolated : Boolean,

    rssPAdESAlgorithm : String | undefined,
    rssXMLAlgorithm : String | undefined,
}

export function isSignatureValid(report : SignatureVerificationReport) {
    return (!report.hasExternalSignatures || !report.externalSignaturesViolated)
        && (!report.hasRSSPAdESSignature || !report.rssPAdESViolated)
        && (!report.hasRSSXMLSignature || !report.rssXMLViolated)
}
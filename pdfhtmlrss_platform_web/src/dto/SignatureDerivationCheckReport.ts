import SignatureVerificationReport from "@/dto/SignatureVerificationReport";

export default interface SignatureDerivationCheckReport {
    redactedDocumentReport : SignatureVerificationReport,
    originalDocumentReport : SignatureVerificationReport,
    isDerived : boolean,
}
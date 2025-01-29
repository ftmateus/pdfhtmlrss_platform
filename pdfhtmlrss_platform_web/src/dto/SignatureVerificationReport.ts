export default interface SignatureVerificationReport {
    padesNotModified: boolean,
    hasRSSSignature : boolean,
    rssNotModified : boolean,
    signatureDate : Date,
    issuedBy : string,
    padesAlgorithm : string,
    rssAlgorithm : string | undefined
}
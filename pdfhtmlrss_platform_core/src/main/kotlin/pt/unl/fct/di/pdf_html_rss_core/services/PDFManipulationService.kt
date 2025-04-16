package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.signatures.SignatureUtil
import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.data.SignatureDerivationCheckReport
import pt.unl.fct.di.pdf_html_rss_core.data.SignatureVerificationReport
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.services.PAdESService.Companion.SIGNATURE_FIELD
import pt.unl.fct.di.pdf_html_rss_core.utils.decompressGZip
import pt.unl.fct.di.pdf_html_rss_core.utils.compressGZip
import java.io.*


@Service
class PDFManipulationService {

    companion object {
        /**
         * Used on better compatibility mode
         */
        const val ATTACHED_FULL_RSS_FILE_NAME = "rss.html.gz"

        /**
         * Used in smaller file size mode
         */
        const val ATTACHED_SHORT_RSS_FILE_NAME = "rss.xml"
    }

    @Autowired
    lateinit var fileConversionService : FileConversionService;

    @Autowired
    lateinit var domService: DOMService;

    @Autowired
    lateinit var pAdESService: PAdESService;

    @Autowired
    lateinit var xhtmlRedactableSignaturesService: XHTMLRedactableSignatureService;

    fun addFileAttachmentsToPdf(pdf : PDFFileWrapper, attachments : Map<String, File>): ByteArray {
        return addAttachmentsToPdf(
            pdf,
            attachments.mapValues { FileInputStream(it.value) }
        );
    }

    fun addByteArrayAttachmentsToPdf(pdf : PDFFileWrapper, attachments : Map<String, ByteArray>): ByteArray {
        return addAttachmentsToPdf(
            pdf,
            attachments.mapValues { ByteArrayInputStream(it.value) }
        );
    }

    fun addAttachmentsToPdf(pdf : PDFFileWrapper, attachments : Map<String, InputStream>) : ByteArray
    {
        val out = ByteArrayOutputStream();
        try {
            pdf.useItextPdfStamper({ stamper ->
                attachments.forEach { (name, attachment) ->
                    val fs: PdfFileSpecification = PdfFileSpecification.fileEmbedded(
                        stamper.writer, null, name, attachment.readBytes()
                    )

                    stamper.addFileAttachment(name, fs);
                }
            }, out);

            return out.toByteArray().also {
                assert(it.isNotEmpty())
            };
        }
        finally {
            out.close()
            attachments.forEach { (_, a) ->  a.close()}
        }
    }

    //https://stackoverflow.com/questions/6305505/how-do-i-extract-attachments-from-a-pdf-file
    fun getAttachments(pdf : PDFFileWrapper) = sequence {
        val embeddedFiles = pdf.useItextPdfReader {
             it.catalog
                ?.getAsDict(PdfName.NAMES)
                ?.getAsDict(PdfName.EMBEDDEDFILES)
                ?.getAsArray(PdfName.NAMES)
        }

        if(embeddedFiles == null || embeddedFiles.isEmpty)
            return@sequence

        for ( i in 0 until embeddedFiles.size() step 2) {
            val name : String = embeddedFiles.getAsString(i).toString()
            val fileSpec = embeddedFiles.getAsDict(i + 1)

            val streams = fileSpec.getAsDict(PdfName.EF)
            val stream = if (streams.contains(PdfName.UF)) streams.getAsStream(PdfName.UF) as PRStream?
            else streams.getAsStream(PdfName.F) as PRStream

            if (stream == null)
                continue

            Pair(name, PdfReader.getStreamBytes(stream as PRStream?))
                .also { yield(it) }
        }
    }

    fun hasRedactableSignature(pdf : PDFFileWrapper) : Boolean {
        return getAttachments(pdf)
            .any { it.first == ATTACHED_FULL_RSS_FILE_NAME }
    }

    fun getRedactableSignature(pdf : PDFFileWrapper) : Document {
        val sigData = getAttachments(pdf)
            .firstOrNull { it.first == ATTACHED_FULL_RSS_FILE_NAME }
            ?.second;

        if(sigData == null)
            throw PDFHTMLRSSException("No redactable signature is present in ${pdf.name}")

        val decompressedSig = sigData.inputStream().use {
            decompressGZip(it)
        }

        //TODO Use Signature XMLRSS class
        return decompressedSig.inputStream().use {
            domService.parseDocument(it)
        }
    }

    fun removeAttachmentFromPdf(pdf : PdfReader, attachmentName : String) : ByteArray {
        throw NotImplementedError();
    }

    //TODO move to another service?
    fun signPdfFileRedactableSignature(pdf : PDFFileWrapper) : PDFFileWrapper {
        val htmlData = fileConversionService.generateHTMLFromPDF(pdf);

        val htmlDom = htmlData.inputStream().use {
            domService.parseDocument(it);
        }

        val signatureDom = xhtmlRedactableSignaturesService.signDocument(htmlDom);

        val compressedHtml = ByteArrayOutputStream().use {
            compressGZip(it) { gzipos ->
                domService.writeDocumentToStream(signatureDom, gzipos)
            }
            it.toByteArray()
        }
        val newPdfData = addAttachmentsToPdf(pdf, mapOf(
            ATTACHED_FULL_RSS_FILE_NAME to compressedHtml.inputStream()
        ));

        return PDFFileWrapper(pdf.name, newPdfData);
    }

    fun verifyPdfFileRedactableSignature(pdf : PDFFileWrapper) : Boolean {
        val signatureDom = getRedactableSignature(pdf)

//        GSRedactableSignature.GSRSSwithBPAccumulatorAndRSA()

        return xhtmlRedactableSignaturesService
            .verifyDocument(signatureDom);
    }

    private fun verifyPAdESSignatureField(sigUtil : SignatureUtil, field : String, coversWholeDocument : Boolean = false) : Boolean {
        val pkcs7 = sigUtil.readSignatureData(field) ?: return false

        if(coversWholeDocument && !sigUtil.signatureCoversWholeDocument(field))
            return false

        return pkcs7.verifySignatureIntegrityAndAuthenticity();
    }

    fun verifyExternalSignatures(sigUtil : SignatureUtil) : SignatureVerificationReport? {
        for(field in sigUtil.signatureNames) {
            if(field == SIGNATURE_FIELD)
                continue;

            if(verifyPAdESSignatureField(sigUtil, field))
                continue;

            //external signature was violated
            return SignatureVerificationReport(
                isSigned = true,
                hasExternalSignatures = true,
                externalSignaturesViolated = true
            )
        }
        return null;
    }

    fun verifyDerivationFromOriginalFile(
        redactedDocument : PDFFileWrapper,
        originalDocument : PDFFileWrapper,
    ) : SignatureDerivationCheckReport {
        val redactedDocReport = verifyPdfDocumentSignatures(redactedDocument);

        val originalDocReport = verifyPdfDocumentSignatures(originalDocument);

        if(!redactedDocReport.isSigned || redactedDocReport.isViolated()
        || !originalDocReport.isSigned || originalDocReport.isViolated())
            return SignatureDerivationCheckReport(redactedDocReport, originalDocReport, false)

        val signatureDom = getRedactableSignature(redactedDocument)

        TODO()
    }

    fun verifyPdfDocumentSignatures(pdf : PDFFileWrapper) : SignatureVerificationReport {
        return pdf.useItextKernelPdfDocument {
            val sigUtil = SignatureUtil(it);

            val signatureFields = sigUtil.signatureNames

            if(signatureFields.isEmpty()) {
                return@useItextKernelPdfDocument SignatureVerificationReport(
                    isSigned = false
                )
            }

            val hasPDFHTMLRSSField =  signatureFields.stream()
                .anyMatch { field -> field == SIGNATURE_FIELD }

            val hasExternalSignatures = !hasPDFHTMLRSSField || signatureFields.size > 1

            if(hasExternalSignatures) {
                val externalSignaturesVerificationReport = verifyExternalSignatures(sigUtil)

                if(externalSignaturesVerificationReport != null)
                    return@useItextKernelPdfDocument externalSignaturesVerificationReport
            }

            if(!hasPDFHTMLRSSField)
                return@useItextKernelPdfDocument SignatureVerificationReport(
                    isSigned = true,
                    hasExternalSignatures = true,
                    externalSignaturesViolated = false,
                    hasRSSPAdESSignature = false
                )

            val rssPAdESpkcs7 = sigUtil.readSignatureData(SIGNATURE_FIELD)
            val rssPAdESAlgorithm = "${rssPAdESpkcs7.signatureAlgorithmName}+${rssPAdESpkcs7.digestAlgorithmName}"

            if(!verifyPAdESSignatureField(sigUtil, SIGNATURE_FIELD, true))
                return@useItextKernelPdfDocument SignatureVerificationReport(
                    isSigned = true,
                    hasExternalSignatures = hasExternalSignatures,
                    externalSignaturesViolated = false,
                    hasRSSPAdESSignature = true,
                    rssPAdESViolated = true,
                    rssPAdESAlgorithm = rssPAdESAlgorithm,
                    issuedBy = rssPAdESpkcs7.signName
                );

            if(!hasRedactableSignature(pdf))
                return@useItextKernelPdfDocument SignatureVerificationReport(
                    isSigned = true,
                    hasExternalSignatures = hasExternalSignatures,
                    externalSignaturesViolated = false,
                    hasRSSPAdESSignature = true,
                    rssPAdESViolated = false,
                    hasRSSXMLSignature = false,
                    rssPAdESAlgorithm = rssPAdESAlgorithm,
                    issuedBy = rssPAdESpkcs7.signName
                );

            val rssXMLSignature = getRedactableSignature(pdf)

            return@useItextKernelPdfDocument SignatureVerificationReport(
                isSigned = true,
                hasExternalSignatures = hasExternalSignatures,
                externalSignaturesViolated = false,
                hasRSSPAdESSignature = true,
                rssPAdESViolated = false,
                hasRSSXMLSignature = true,
                rssPAdESAlgorithm = rssPAdESAlgorithm,
                //TODO
                rssXMLAlgorithm = null,
                rssXMLViolated = !xhtmlRedactableSignaturesService.verifyDocument(rssXMLSignature)
            );
        }
    }
}
package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.pdf.PRStream
import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfReader
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.grss.GSRedactableSignature
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.grss.xml.GSRedactableXMLSignature
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.grss.xml.GSSignatureValue
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.binding.SignatureValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
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
    private lateinit var compressionService: CompressionService

    @Autowired
    lateinit var fileConversionService : FileConversionService;

    @Autowired
    lateinit var domService: DOMService;

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
            .first { it.first == ATTACHED_FULL_RSS_FILE_NAME }
            .second;

        if(sigData == null)
            throw PDFHTMLRSSException()

        val decompressedSig = sigData.inputStream().use {
            compressionService.decompressGZip(it)
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
            compressionService.compressGZip(it) { gzipos ->
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
}
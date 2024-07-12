package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfReader
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.*


@Service
class PDFManipulationService {
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
            pdf.close()
            out.close()
            attachments.forEach { (_, a) ->  a.close()}
        }
    }

    fun getAttachments(pdf : PdfReader) : Map<String, ByteArray> {

//        val pdfDoc =

        throw NotImplementedError();
    }

    fun removeAttachmentFromPdf(pdf : PdfReader, attachmentName : String) : ByteArray {
        throw NotImplementedError();
    }
}
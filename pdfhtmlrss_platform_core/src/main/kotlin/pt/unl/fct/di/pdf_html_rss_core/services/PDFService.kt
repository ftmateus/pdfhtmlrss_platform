package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.pdf.PdfFileSpecification
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import org.springframework.stereotype.Service
import java.io.*


@Service
class PDFService {
    fun addFileAttachmentsToPdf(pdf : PdfReader, attachments : Map<String, File>): ByteArray {
        return _addAttachmentsToPdf(
            pdf,
            attachments.mapValues { it -> FileInputStream(it.value) }
        );
    }

    fun addByteArrayAttachmentsToPdf(pdf : PdfReader, attachments : Map<String, ByteArray>): ByteArray {
        return _addAttachmentsToPdf(
            pdf,
            attachments.mapValues { it -> ByteArrayInputStream(it.value) }
        );
    }

    private fun _addAttachmentsToPdf(pdf : PdfReader, attachments : Map<String, InputStream>) : ByteArray
    {
        val out = ByteArrayOutputStream();
        lateinit var stamper : PdfStamper;
        try {
            stamper = PdfStamper(pdf, out);

            attachments.forEach { (name, attachment) ->
                val fs: PdfFileSpecification = PdfFileSpecification.fileEmbedded(
                    stamper.writer, null, name,
                    "Test".toByteArray()
                )

                stamper.addFileAttachment("Test", fs);
            }

            return out.toByteArray().also {
                assert(it.isNotEmpty())
            };
        }
        finally {
            stamper.close();
            pdf.close()
            out.close()
            attachments.forEach { (_, a) ->  a.close()}
        }
    }
}
package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.fit.pdfdom.PDFDomTree
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.encodingWith

@Service
class PDFConversionService {

    @Autowired
    lateinit var domService: DOMService;

    fun generateHTMLFromPDFLinux(fileData: ByteArray) : ByteArray {
        val pdfToHtmlProcess = ProcessBuilder()
            .command("/usr/bin/pdftohtml", "-s", "-q", "-stdout", "-dataurls", "-", "-")
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start();

        pdfToHtmlProcess.outputStream.use {
            it.write(fileData);
        }

        return pdfToHtmlProcess.inputStream.use {
            it.readBytes();
        }
    }

    @Deprecated("")
    fun generateHTMLFromPDFApachePDF(fileData : ByteArray) : ByteArray
    {
        val pdf = PDDocument.load(fileData);
        val output = ByteArrayOutputStream()
        val printWriter = PrintWriter(
            output,
            false, StandardCharsets.ISO_8859_1
        );


        printWriter.use {
            val pdfDomTree = PDFDomTree()
            pdfDomTree.startDocument(pdf);
            pdfDomTree.writeText(pdf, printWriter)
        }

        return replaceInvalidCharacters(output.toByteArray())
    }

    fun generateHTMLFromPDF(filePath : String, destination : String = "$filePath.html") {
        val pdfBytes = generateHTMLFromPDFLinux(File(filePath).readBytes());
        FileOutputStream(File(destination)).write(pdfBytes)
    }

    private fun replaceInvalidCharacters(bytes : ByteArray) : ByteArray
    {
        val content = String(bytes).replace("&nbsp;", "&#160;")
        return content.toByteArray()
    }

    private fun replaceInvalidCharacters(htmlFilePath : String)
    {
        val html = File(htmlFilePath)
        var content = String(html.readBytes())
        content = content.replace("&nbsp;", "&#160;")
        val out = FileOutputStream(htmlFilePath)
        out.use {
            out.write(content.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun generatePDFFromHTML(domDoc : org.w3c.dom.Document) : ByteArray
    {
        ByteArrayOutputStream().use { out ->
            val renderer = ITextRenderer()
            renderer.sharedContext.apply {
                this.setPrint(true)
                this.isInteractive = false
            }
            with(renderer) {
                this.setDocument(domDoc, "")
                this.layout()
                this.createPDF(out)
                this.finishPDF()
            }
            return out.toByteArray()
        }
    }

    fun generatePDFFromHTMLOld(domDoc : org.w3c.dom.Document) : ByteArray {
        val pdfDoc : com.itextpdf.text.Document = Document();

        val pdfOut = ByteArrayOutputStream();

        val writer : PdfWriter = PdfWriter.getInstance(
            pdfDoc,
            pdfOut
        );

        val domDocBytes = domService.convertDomDocumentToByteArray(domDoc);

        pdfDoc.open();
        XMLWorkerHelper.getInstance()
            .parseXHtml(writer, pdfDoc, ByteArrayInputStream(domDocBytes), StandardCharsets.UTF_8)
        pdfDoc.close();

        return pdfOut.toByteArray();
    }

    fun generatePDFFromHTMLOld(file : File, destination: String = "${file.absolutePath}.pdf") {
        if(file.extension != "html" && file.extension != "xml")
            return;
        val document : Document = Document();
        val writer : PdfWriter = PdfWriter.getInstance(
            document,
            FileOutputStream(File(destination))
        );
        document.open();
        XMLWorkerHelper.getInstance()
            .parseXHtml(writer, document, FileInputStream(file), StandardCharsets.UTF_8)
        document.close();
    }
}
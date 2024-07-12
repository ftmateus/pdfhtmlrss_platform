package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.fit.pdfdom.PDFDomTree
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xhtmlrenderer.pdf.ITextRenderer
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.*
import java.nio.charset.StandardCharsets

@Service
class FileConversionService {

    @Autowired
    lateinit var domService: DOMService;

    fun generateHTMLFromPDFLinux(pdf: PDFFileWrapper) : ByteArray {
        val pdfToHtmlProcess = ProcessBuilder()
            .command("/usr/bin/pdftohtml", "-s", "-q", "-stdout", "-dataurls", "-", "-")
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start();

        pdfToHtmlProcess.outputStream.use {
            it.write(pdf.getData());
        }

        return pdfToHtmlProcess.inputStream.use {
            it.readBytes();
        }.also { assert(it.isNotEmpty()) }
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

    fun generateHTMLFromPDF(pdfFile : PDFFileWrapper) : ByteArray {
        return generateHTMLFromPDFLinux(pdfFile)
    }

//    fun generateHTMLFromPDF(filePath : String, destination : String = "$filePath.html") {
//        val pdf = generateHTMLFromPDFLinux(File(filePath).readBytes());
//        FileOutputStream(File(destination)).write(pdfBytes)
//    }

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
        FileOutputStream(htmlFilePath).use {
            it.write(content.toByteArray(StandardCharsets.UTF_8))
        }
    }

    fun generatePDFFromHTML(domDocData : ByteArray) : PDFFileWrapper  {
        val parsedDoc : org.w3c.dom.Document = ByteArrayInputStream(domDocData).use {
            domService.parseDocument(it)
        }

        return generatePDFFromHTML(parsedDoc);
    }

    fun generatePDFFromHTML(domDoc : File) : PDFFileWrapper {
        val parsedDoc : org.w3c.dom.Document  = domService.parseDocument(domDoc);
        return generatePDFFromHTML(parsedDoc);
    }

    fun generatePDFFromHTML(domDoc : org.w3c.dom.Document) : PDFFileWrapper
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
            return PDFFileWrapper("", out.toByteArray())
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
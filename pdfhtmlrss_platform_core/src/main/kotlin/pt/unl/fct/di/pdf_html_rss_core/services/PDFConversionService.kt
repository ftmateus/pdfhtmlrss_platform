package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.XMLWorkerHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.fit.pdfdom.PDFDomTree
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.StandardCharsets

@Service
class PDFConversionService {

    fun generateHTMLFromPDF(filePath : String, destination : String = "$filePath.html") : Unit {
        val file = File(filePath);
        val pdf : PDDocument = PDDocument.load(file);
        val output : Writer = PrintWriter(destination, StandardCharsets.UTF_8.toString());
        output.use {
            val pdfDomTree = PDFDomTree()
            pdfDomTree.writeText(pdf, output)
            pdfDomTree
        }
        replaceInvalidCharacters(destination);
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

    fun generatePDFFromHTML(filename : String) {
        val document : Document = Document();
        val writer : PdfWriter = PdfWriter.getInstance(document, FileOutputStream("html.pdf"));
        document.open();
        XMLWorkerHelper.getInstance()
            .parseXHtml(writer, document, FileInputStream(filename), StandardCharsets.UTF_8)
        document.close();
    }
}
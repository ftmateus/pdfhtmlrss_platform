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

    fun generateHTMLFromPDF(filename : String) : Unit {
        val pdf : PDDocument = PDDocument.load(File(filename));
        val output : Writer = PrintWriter("$filename.html", StandardCharsets.UTF_8.toString());
        val pdfDomTree = PDFDomTree();
        pdfDomTree.writeText(pdf, output)

        output.close();
    }

    fun generatePDFFromHTML(filename : String) {
        val document : Document = Document();
        val writer : PdfWriter = PdfWriter.getInstance(document, FileOutputStream("html.pdf"));
        document.open();
        XMLWorkerHelper.getInstance()
            .parseXHtml(writer, document, FileInputStream(filename));
        document.close();
    }
}
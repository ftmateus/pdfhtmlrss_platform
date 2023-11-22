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


    fun generateHTMLFromPDF(fileData : ByteArray) : ByteArray
    {
        val pdf = PDDocument.load(fileData);
        val output = ByteArrayOutputStream()
        val printWriter = PrintWriter(
            output,
            false
        );


        printWriter.use {
            val pdfDomTree = PDFDomTree()
            pdfDomTree.startDocument(pdf);
//            val doctype = pdfDomTree.document.doctype
//            pdfDomTree.document.implementation.createDocumentType(
//                "html",
//                "-//W3C//DTD XHTML 1.0 Strict//EN",
//                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
//            )
//            pdfDomTree.document.implementation.cre
            pdfDomTree.writeText(pdf, printWriter)
//            doctype.textContent
        }

        return replaceInvalidCharacters(output.toByteArray())
    }

    fun generateHTMLFromPDF(filePath : String, destination : String = "$filePath.html") {
        val pdfBytes = generateHTMLFromPDF(File(filePath).readBytes());
        FileOutputStream(File(destination)).write(pdfBytes)
    }

    fun generateHTMLFromPDFOld(filePath : String, destination : String = "$filePath.html") : Unit {
        val file = File(filePath);
        val pdf : PDDocument = PDDocument.load(file);
        val output : Writer = PrintWriter(destination, StandardCharsets.UTF_8.toString());
        output.use {
            val pdfDomTree = PDFDomTree()
            pdfDomTree.startDocument(pdf);
//            val doctype = pdfDomTree.document.doctype
//            pdfDomTree.document.implementation.createDocumentType(
//                "html",
//                "-//W3C//DTD XHTML 1.0 Strict//EN",
//                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
//            )
//            pdfDomTree.document.implementation.cre
            pdfDomTree.writeText(pdf, output)
//            doctype.textContent
        }
        replaceInvalidCharacters(destination);
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

    fun generatePDFFromHTML(file : File, destination: String = "${file.absolutePath}.pdf") {
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
package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.xhtmlrenderer.pdf.ITextRenderer
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.*
import java.nio.charset.StandardCharsets
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter
import org.w3c.dom.html.HTMLStyleElement

@Service
class FileConversionService {

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    lateinit var domService: DOMService;

    @Deprecated("")
    fun generateHTMLFromPDF(pdf: PDFFileWrapper) : ByteArray {
        val domDoc = generateHTMLFromPDFDoc(pdf)

        return domService.convertDomDocumentToByteArray(domDoc)
    }

    fun generateHTMLFromPDFDoc(pdfFile: PDFFileWrapper) : Document {
        val domDoc = when(pdfFile.numberOfPages) {
            1 -> generateHTMLFromPDFSinglePage(pdfFile)
            else -> generateHTMLFromPDFDocMultiplePages(pdfFile)
        }

        domService.cleanDocument(domDoc)

        return domDoc
    }

    private fun generateHTMLFromPDFSinglePage(pdfFile : PDFFileWrapper) : Document {
        assert(pdfFile.numberOfPages == 1)

        val pdfToHtmlProcess = ProcessBuilder()
            .command("/usr/bin/pdftohtml", "-s", "-q", "-stdout", "-dataurls", "-zoom", "1.12", "-p", "-nomerge", "-noframes", "-", "-")
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start();

        val tidyProcess = ProcessBuilder()
            .command("/usr/bin/tidy", "-q", "-c", "-m", "-utf8",
                "--numeric-entities", "yes",
                "--drop-proprietary-attributes", "yes",
                "--drop-empty-elements", "no",
                "--doctype", "html5",
//                "--fix-bad-comments", "yes",
//                "--hide-comments", "yes",
                "--quote-nbsp", "no")
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start()

        pdfFile.getInputStream().use { inS ->
            tidyProcess.outputStream.use {
                pdfToHtmlProcess.outputStream.use { outS ->
                    inS.copyTo(outS)
                }
                pdfToHtmlProcess.inputStream.copyTo(it)
            }
        }

        try {
            return tidyProcess.inputStream.use {
                domService.parseDocument(it, true)
            }
        } catch (ex: Exception) {
            throw PDFHTMLRSSException("PDF to HTML conversion error.\npdftohtml exit code: ${pdfToHtmlProcess.exitValue()};\ntidy exit code: ${tidyProcess.exitValue()} ", ex);
        }
    }

    fun generateHTMLFromPDFDocMultiplePages(pdfFile : PDFFileWrapper) : Document {
        val tmpFile = temporaryFilesRepository.getNewTmpFileWithoutCreating(".html");
        try {
            val pdfToHtmlProcess = ProcessBuilder()
                .command("/usr/bin/pdftohtml", "-c", "-q", "-dataurls", "-zoom", "1.12", "-noframes", "-", tmpFile.path)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start()

            pdfFile.getInputStream().use { inS ->
                pdfToHtmlProcess.outputStream.use { outS ->
                    inS.copyTo(outS)
                }
            }

            pdfToHtmlProcess.waitFor()

            val tidyProcess = ProcessBuilder()
                .command(
                    "/usr/bin/tidy", "-q",
                    "--numeric-entities", "yes",
                    "-output", "/dev/stdout",
                    tmpFile.path
                )
                .start()

            val domDoc = tidyProcess.inputStream.use {
                domService.parseDocument(it, true)
            }

            tidyProcess.waitFor();

            return domDoc;
        }
        finally {
            tmpFile.delete()
        }
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
                this.isPrint = true
//                this.dpi *= 1.20f;
//                this.
                this.isInteractive = false
            }
            with(renderer) {
//                val firstPageNode = domDoc.getElementById("page1-div")
                this.setDocument(domDoc, "")
                this.layout()
                this.createPDF(out)

                this.finishPDF()
            }
            return PDFFileWrapper("", out.toByteArray())
        }
    }
}
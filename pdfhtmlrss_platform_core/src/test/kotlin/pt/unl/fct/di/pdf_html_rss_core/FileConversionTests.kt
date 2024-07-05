package pt.unl.fct.di.pdf_html_rss_core

import com.itextpdf.text.pdf.PdfReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


@SpringBootTest
class FileConversionTests {
    val temporaryFolder = createTemporaryTestFolder();

    @Autowired
    lateinit var pdfConversionService: PDFConversionService;

    @Autowired
    lateinit var domService: DOMService;


    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFiles"])
    fun htmlToPdf(htmlFile : File) {

		val pdfDocData = pdfConversionService.generatePDFFromHTML(htmlFile)

        writeDataToTempFile(
            pdfDocData,
            temporaryFolder,
            "${htmlFile.name}.pdf"
        )

        assertTrue(pdfDocData.isNotEmpty());
        assertIsValidPdfFile(pdfDocData);
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun pdfToHtml(pdfFile : File) {

        val htmlDocData = pdfConversionService.generateHTMLFromPDF(pdfFile)

        writeDataToTempFile(
            htmlDocData,
            temporaryFolder,
            "${pdfFile.name}.html"
        )

        assertTrue(htmlDocData.isNotEmpty())

        assertIsValidHtmlFile(htmlDocData);
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun pdfReconversion(pdfFile: File) {

        val htmlDocData = pdfConversionService.generateHTMLFromPDF(pdfFile)

        val reconvertedPdfDocData = pdfConversionService.generatePDFFromHTML(htmlDocData)

        writeDataToTempFile(
            reconvertedPdfDocData,
            temporaryFolder,
            "${pdfFile.name}.html.pdf"
        )

        assertIsValidPdfFile(reconvertedPdfDocData);

        val parsedPdf = FileInputStream(pdfFile).use {
            PdfReader(it)
        }

        val parsedReconvertedPdf = ByteArrayInputStream(reconvertedPdfDocData).use {
            PdfReader(it)
        }

        try {
            //FIXME reconverted PDF generating more (blank) pages than it should
            assertEquals(parsedPdf.numberOfPages, parsedReconvertedPdf.numberOfPages);
        } finally {
            parsedPdf.close();
            parsedReconvertedPdf.close()
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun `Check if HTML to PDF conversion is idempotent enough`(pdfFile: File) {
        val htmlDocData = pdfConversionService.generateHTMLFromPDF(pdfFile)

        val parsedReconvertedPdf1 = pdfConversionService.generatePDFFromHTML(htmlDocData).let {
            ByteArrayInputStream(it).use { bais ->
                PdfReader(bais)
            }
        }

        val parsedReconvertedPdf2 = pdfConversionService.generatePDFFromHTML(htmlDocData).let {
            ByteArrayInputStream(it).use { bais ->
                PdfReader(bais)
            }
        }

        assertPdfFilesAreSame(parsedReconvertedPdf1, parsedReconvertedPdf2);
    }

    fun assertIsValidPdfFile(pdfData : ByteArray) {
        val pdfReader : PdfReader = assertDoesNotThrow {
            PdfReader(pdfData)
        }
        try {
            assert(pdfReader.fileLength > 0);
            assert(pdfReader.numberOfPages > 0);
        }
        finally {
            pdfReader.close();
        }
    }


    fun assertIsValidHtmlFile(htmlData : ByteArray) {
        val doc = assertDoesNotThrow {
            ByteArrayInputStream(htmlData).use {
                domService.parseDocument(it);
            }
        }
    }

    fun assertPdfFilesAreSame(pdf1 : PdfReader, pdf2 : PdfReader) {
        assertEquals(pdf1.fileLength, pdf2.fileLength)
        assertEquals(pdf1.numberOfPages, pdf2.numberOfPages)
        assertEquals(pdf1.pdfVersion, pdf2.pdfVersion)
        assertEquals(pdf1.eofPos, pdf2.eofPos)
        assertEquals(pdf1.metadata, pdf2.metadata)
//        assertEquals(pdf1.info, pdf2.info)

        for (p in 1 .. pdf1.numberOfPages) {
            val pdf1PageContent = pdf1.getPageContent(p)
            val pdf2PageContent = pdf2.getPageContent(p)

            assertEquals(pdf1PageContent.size, pdf2PageContent.size)
            assertEquals(pdf1PageContent.toList(), pdf2PageContent.toList())
        }
    }
}
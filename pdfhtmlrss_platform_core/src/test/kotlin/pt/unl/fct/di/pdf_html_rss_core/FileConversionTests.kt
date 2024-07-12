package pt.unl.fct.di.pdf_html_rss_core

import com.itextpdf.text.pdf.PdfReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.FileConversionService
import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import java.io.ByteArrayInputStream
import java.io.File


@SpringBootTest
class FileConversionTests {
    @Autowired
    private lateinit var securityService: SecurityService
    val temporaryFolder = createTemporaryTestFolder();

    @Autowired
    lateinit var pdfConversionService: FileConversionService;

    @Autowired
    lateinit var domService: DOMService;


    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFiles"])
    fun htmlToPdf(htmlFile: File) {

        val pdf = pdfConversionService.generatePDFFromHTML(htmlFile)

        writeDataToTempFile(
            pdf.getData(),
            temporaryFolder,
            "${htmlFile.name}.pdf"
        )

        assertTrue(pdf.getData().isNotEmpty());
        assertIsValidPdfFile(pdf);
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun pdfToHtml(pdfFile : PDFFileWrapper) {

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
    fun pdfReconversion(pdfFile: PDFFileWrapper) {

        val htmlDocData = pdfConversionService.generateHTMLFromPDF(pdfFile)

        val reconvertedPdfDoc = pdfConversionService.generatePDFFromHTML(htmlDocData)

        writeDataToTempFile(
            reconvertedPdfDoc.getData(),
            temporaryFolder,
            "${pdfFile.name}.html.pdf"
        )

        assertIsValidPdfFile(reconvertedPdfDoc);

        //FIXME reconverted PDF generating more (blank) pages than it should
        assertEquals(
            pdfFile.numberOfPages,
            reconvertedPdfDoc.numberOfPages
        );
    }


    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun pdfToHtmlIdempotencyTest(pdfFile: PDFFileWrapper) {
        val htmlDocData1 = pdfConversionService.generateHTMLFromPDF(pdfFile)
        val htmlDocData2 = pdfConversionService.generateHTMLFromPDF(pdfFile)

        val htmlDocHash1 = securityService.toSha256(htmlDocData1)
        val htmlDocHash2 = securityService.toSha256(htmlDocData2)

        assertEquals(htmlDocHash1, htmlDocHash2)
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun `Check if HTML to PDF conversion is idempotent enough`(pdfFile: PDFFileWrapper) {
        val htmlDocData = pdfConversionService.generateHTMLFromPDF(pdfFile)

        val parsedReconvertedPdf1 = pdfConversionService
            .generatePDFFromHTML(htmlDocData)

        val parsedReconvertedPdf2 = pdfConversionService
            .generatePDFFromHTML(htmlDocData)

        assertPdfFilesAreSame(parsedReconvertedPdf1, parsedReconvertedPdf2);
    }

    fun assertIsValidPdfFile(pdfFile: PDFFileWrapper) {
        assertDoesNotThrow {
            pdfFile.useItextPdfReader {
                assert(it.fileLength > 0);
                assert(it.numberOfPages > 0);
            }
        }
    }


    fun assertIsValidHtmlFile(htmlData : ByteArray) {
        val doc = assertDoesNotThrow {
            ByteArrayInputStream(htmlData).use {
                domService.parseDocument(it);
            }
        }
    }

    fun assertPdfFilesAreSame(pdf1 : PDFFileWrapper, pdf2 : PDFFileWrapper) {

        assertEquals(pdf1.fileLength, pdf2.fileLength)
        assertEquals(pdf1.numberOfPages, pdf2.numberOfPages)
        assertEquals(pdf1.pdfVersion, pdf2.pdfVersion)

        assertEquals(pdf1.useItextPdfReader { it.eofPos }, pdf2.useItextPdfReader { it.eofPos })
//            assertEquals(pdf1.metadata, pdf2.metadata)
//        assertEquals(pdf1.info, pdf2.info)

        for (p in 1 .. pdf1.numberOfPages) {
            val pdf1PageContent = pdf1.getPageContent(p)
            val pdf2PageContent = pdf2.getPageContent(p)

            assertEquals(pdf1PageContent.size, pdf2PageContent.size)
            assertEquals(pdf1PageContent.toList(), pdf2PageContent.toList())
        }
    }
}
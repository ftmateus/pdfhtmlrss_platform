package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.PDFService
import java.io.File

@SpringBootTest
class PDFManipulationTests {

    val temporaryFolder = createTemporaryTestFolder();

    @Autowired
    lateinit var pdfService: PDFService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun testPdfFileWrapperFetchMetadata(pdfFileWrapper: PDFFileWrapper) {
        assertDoesNotThrow {
            pdfFileWrapper.numberOfPages;
            pdfFileWrapper.pdfVersion
            pdfFileWrapper.fileLength
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun pdfAttachments(pdfFile : PDFFileWrapper) {
        val attachments = mapOf(
            "Test1.txt" to "Hello World".toByteArray(),
            "Test2.txt" to "BOOOOO".toByteArray()
        )

        val newPdfData = pdfService.addByteArrayAttachmentsToPdf(pdfFile, attachments);

        writeDataToTempFile(newPdfData, temporaryFolder, "${pdfFile.name}_attachments.pdf")

        //TODO check if attachment is present
    }




}
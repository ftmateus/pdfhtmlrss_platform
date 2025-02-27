package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.PDFManipulationService
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository

@SpringBootTest
class PDFManipulationTests {

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @Autowired
    lateinit var pdfManipulationService: PDFManipulationService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun testPdfFileWrapperFetchMetadata(pdfFileWrapper: PDFFileWrapper) {
        assertDoesNotThrow {
            pdfFileWrapper.numberOfPages;
            pdfFileWrapper.pdfVersion
            pdfFileWrapper.fileLength
        }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun pdfAttachments(pdfFile : PDFFileWrapper) {
        val attachmentsToAdd = mapOf(
            "Test1.txt" to "Hello World".toByteArray(),
            "Test2.txt" to "BOOOOO".toByteArray()
        )

        val newPdfData = pdfManipulationService.addByteArrayAttachmentsToPdf(pdfFile, attachmentsToAdd);

        temporaryFilesRepository.writeToTempFile(
            newPdfData.inputStream(),
            "${pdfFile.name}_attachments.pdf",
            deleteAutomatically = false
        )

        val attachmentsReturned = pdfManipulationService
            .getAttachments(PDFFileWrapper("", newPdfData))
            .toMap()

        assertTrue(attachmentsReturned.isNotEmpty())
        assertTrue(attachmentsReturned.keys.containsAll(attachmentsToAdd.keys))

        //TODO verify attachments data
    }
}
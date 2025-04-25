package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getRedactSelectors
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.data.RedactionProcess
import pt.unl.fct.di.pdf_html_rss_core.data.RedactionProcessAction
import pt.unl.fct.di.pdf_html_rss_core.repositories.RedactionProcessRepository
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.RedactionProcessService
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.PDFManipulationService
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.BeforeTest

@SpringBootTest
@WithUserDetails("admin")
class RedactionProcessTests {

    @Autowired
    lateinit var redactionProcessService: RedactionProcessService

    @Autowired
    lateinit var redactionProcessRepository: RedactionProcessRepository

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @Autowired
    lateinit var pdfManipulationService: PDFManipulationService

    @Autowired
    lateinit var domService : DOMService;

    lateinit var testResultsSubFolder : File;

    @BeforeTest
    fun createSubFolder() {
        testResultsSubFolder = temporaryFilesRepository.makeTempSubFolder("redaction-process-tests")
    }

    fun testPdfGenericRedactionProcess(pdfFile : PDFFileWrapper,
        elementsToRedact : List<String>, action : RedactionProcessAction) : PDFFileWrapper {
        val selectRedactableElemsProcess = redactionProcessService.initiatePdfRedactionProcess(
            pdfFile,
            action
        )

        assertPdfRedactionProcessIsValid(selectRedactableElemsProcess)

        val rssSignedFilePath = when(action) {
            RedactionProcessAction.SELECT_REDACTABLE_ELEMS -> "${testResultsSubFolder.name}/${pdfFile.name}_signed_rss.pdf"
            RedactionProcessAction.REDACT -> "${testResultsSubFolder.name}/${pdfFile.name}_redacted_rss.pdf"
        };

        val rssSignedFile = temporaryFilesRepository.writeToTempFile(rssSignedFilePath, deleteAutomatically = false) {
            val p = redactionProcessService.finalizeRedactionProcess(
                selectRedactableElemsProcess.taskId,
                elementsToRedact,
                it
            )
            assertEquals(p, selectRedactableElemsProcess)
        }.let { PDFFileWrapper(it) }

        pdfManipulationService.verifyPdfDocumentSignatures(rssSignedFile)
        .also {
            assertTrue(!it.isViolated())
            assertTrue(it.hasRSSPAdESSignature)
            assertTrue(it.hasRSSXMLSignature)
        }

        return rssSignedFile;
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFilesToRedact"])
    fun `Test PDF redaction process (sign only)`(pdfFileName: String) {
        val pdfFile = PDFFileWrapper(
            getTestFile(pdfFileName)
        );

        val elementsToRedact = getRedactSelectors(pdfFileName)

        val rssSignedPdfFile = testPdfGenericRedactionProcess(
            pdfFile, elementsToRedact, RedactionProcessAction.SELECT_REDACTABLE_ELEMS)

    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFilesToRedact"])
    fun `Test PDF redaction process (sign and redact)`(pdfFileName: String) {
        val pdfFile = PDFFileWrapper(
            getTestFile(pdfFileName)
        );

        val elementsToRedact = getRedactSelectors(pdfFileName)

        val rssSignedPdfFile = testPdfGenericRedactionProcess(
            pdfFile, elementsToRedact, RedactionProcessAction.SELECT_REDACTABLE_ELEMS)


        val rssRedactedPdfFile = testPdfGenericRedactionProcess(
            rssSignedPdfFile, elementsToRedact, RedactionProcessAction.REDACT)
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFilesToRedact"])
    fun `Test HTML redaction process (sign and redact)`(htmlFileName: String, elementsToRedact : List<String>) {

        val htmlFile = getTestFile(htmlFileName)

        val selectRedactableElemsProcess = htmlFile.inputStream().use {
             redactionProcessService.initiateXHtmlRedactionProcess(
                it,
                RedactionProcessAction.SELECT_REDACTABLE_ELEMS
            )
        }

        assertHtmlRedactionProcessIsValid(selectRedactableElemsProcess)

        val processedDoc = ByteArrayOutputStream().use { out ->
            val p = redactionProcessService.finalizeRedactionProcess(
                selectRedactableElemsProcess.taskId,
                elementsToRedact,
                out
            )
            assertEquals(p, selectRedactableElemsProcess)
            out.toByteArray()
        }

        assertRedactionProcessWasFinished(selectRedactableElemsProcess)

        temporaryFilesRepository.writeToTempFile(htmlFileName + "_unredacted.html", deleteAutomatically = false) {
            processedDoc.inputStream().copyTo(it)
        }

        assertTrue(processedDoc.isNotEmpty())

        val redactProcess = processedDoc.inputStream().use {
            redactionProcessService.initiateXHtmlRedactionProcess(
                it,
                RedactionProcessAction.REDACT
            )
        }

        assertNotEquals(selectRedactableElemsProcess, redactProcess)

        assertHtmlRedactionProcessIsValid(redactProcess)

        val redactedDoc = ByteArrayOutputStream().use { out ->
            val p = redactionProcessService.finalizeRedactionProcess(
                redactProcess.taskId,
                elementsToRedact,
                out
            )
            assertEquals(p, redactProcess)
            out.toByteArray()
        }

        assertTrue(processedDoc.isNotEmpty())
        assertDoesNotThrow {
            processedDoc.inputStream().use {
                domService.parseDocument(it)
            }
        }
    }

    fun assertHtmlRedactionProcessIsValid(process: RedactionProcess) {
        assertTrue(
            temporaryFilesRepository.getTempFile(process.tmpHtmlFile).let {
                it != null && it.exists() && it.isFile
            }
        )

        assertTrue(
            process.tmpPdfFile == null
        )

        assertNotNull(
            redactionProcessRepository.findById(process.taskId)
                .orElse(null)
        )
    }

    fun assertPdfRedactionProcessIsValid(process: RedactionProcess) {
        assertTrue(
            temporaryFilesRepository.getTempFile(process.tmpHtmlFile).let {
                it != null && it.exists() && it.isFile
            }
        )

        assertTrue(
            process.tmpPdfFile != null &&
            temporaryFilesRepository.getTempFile(process.tmpPdfFile!!).let {
                it != null && it.exists() && it.isFile
            }
        )

        assertNotNull(
            redactionProcessRepository.findById(process.taskId)
                .orElse(null)
        )
    }

    fun assertRedactionProcessWasFinished(process: RedactionProcess) {
        assertFalse(
            temporaryFilesRepository.getTempFile(process.tmpHtmlFile)
                ?.exists() ?: false
        )

        assertFalse(
            process.tmpPdfFile != null &&
            temporaryFilesRepository.getTempFile(process.tmpPdfFile!!).let {
                it != null && it.exists() && it.isFile
            }
        )

        assertNull(
            redactionProcessRepository.findById(process.taskId)
                .orElse(null)
        )
    }
}
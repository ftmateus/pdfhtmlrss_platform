package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcess
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcessAction
import pt.unl.fct.di.pdf_html_rss_core.repositories.RedactionProcessRepository
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.RedactionProcessService
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.ByteArrayOutputStream

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
    lateinit var domService : DOMService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFilesToRedact"])
    fun testPdfRedactionProcess(pdfFileName: String, elementsToRedact : List<String>) {
        val pdfFile = PDFFileWrapper(
            getTestFile(pdfFileName)
        );

        val selectRedactableElemsProcess = redactionProcessService.initiatePdfRedactionProcess(
            pdfFile,
            RedactionProcessAction.SELECT_REDACTABLE_ELEMS
        )

        assertPdfRedactionProcessIsValid(selectRedactableElemsProcess)

        ByteArrayOutputStream().use { out ->
            val p = redactionProcessService.finalizeRedactionProcess(
                selectRedactableElemsProcess.taskId,
                elementsToRedact,
                out
            )
            assertEquals(p, selectRedactableElemsProcess)
        }

        assertRedactionProcessWasFinished(selectRedactableElemsProcess)
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFilesToRedact"])
    fun testHtmlRedactionProcess(htmlFileName: String, elementsToRedact : List<String>) {

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
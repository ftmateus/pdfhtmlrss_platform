package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.PAdESService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFManipulationService
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

@SpringBootTest
@WithUserDetails("admin")
class PAdESTests {

    @Autowired
    private lateinit var pAdESService: PAdESService;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @Autowired
    private lateinit var pdfManipulationService: PDFManipulationService;

    lateinit var testResultsSubFolder : File;

    @BeforeTest
    fun createSubFolder() {
        testResultsSubFolder = temporaryFilesRepository.makeTempSubFolder("pades-tests")
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun `Sign and verify PDF files`(pdfFile : PDFFileWrapper) {
        val signedFilePath = "${testResultsSubFolder.name}/${pdfFile.name}_signed.pdf"

        val signedFile = temporaryFilesRepository.writeToTempFile(signedFilePath, false) { out ->
            pAdESService.signDocument(pdfFile, out)
        }.let { PDFFileWrapper(it) }

        pdfManipulationService.verifyPdfDocumentSignatures(signedFile)
            .also {
                assertTrue(it.isSigned)
                assertTrue(it.hasRSSPAdESSignature)
                assertFalse(it.hasExternalSignatures)
                assertFalse(it.hasRSSXMLSignature)
                assertFalse(it.isViolated())
            }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun failOnNotSignedDocument(pdfFile : PDFFileWrapper) {
        pAdESService.verifyDocument(pdfFile)
            .also { assertFalse(it) }
    }
}
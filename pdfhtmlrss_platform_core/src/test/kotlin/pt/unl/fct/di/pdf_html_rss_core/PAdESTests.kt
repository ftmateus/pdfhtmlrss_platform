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
import kotlin.test.assertTrue

@SpringBootTest
@WithUserDetails("admin")
class PAdESTests {

    @Autowired
    private lateinit var pAdESService: PAdESService;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun signAndVerifyPdfFiles(pdfFile : PDFFileWrapper) {
        val signedFile = temporaryFilesRepository.writeToTempFile("${pdfFile.name}_signed.pdf", false) { out ->
            pAdESService.signDocument(pdfFile, out)
        }

        pAdESService.verifyDocument(PDFFileWrapper(signedFile))
            .also { assertTrue(it) }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun failOnNotSignedDocument(pdfFile : PDFFileWrapper) {
        pAdESService.verifyDocument(pdfFile)
            .also { assertFalse(it) }
    }
}
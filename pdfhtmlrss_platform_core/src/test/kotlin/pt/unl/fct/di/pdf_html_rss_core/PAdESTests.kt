package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.PAdESService

@SpringBootTest
@WithUserDetails("admin")
class PAdESTests {

    @Autowired
    private lateinit var pAdESService: PAdESService;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun test1(pdfFile : PDFFileWrapper) {
        temporaryFilesRepository.writeToTempFile("${pdfFile.name}_signed.pdf", false) { out ->
            pAdESService.signDocument(pdfFile, out)
        }
    }

}
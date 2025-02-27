package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.*

@SpringBootTest
@WithUserDetails("admin")
class RedactionTests {

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @Autowired
    lateinit var fileConversionService: FileConversionService;

    @Autowired
    lateinit var redactableSignaturesService: XHTMLRedactableSignatureService;

    @Autowired
    lateinit var domService: DOMService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFilesToRedact"])
    fun redactXPathHtml(fileName: String, xpathElems: List<String>) {
        val htmlFile = getTestFile(fileName);

        val htmlDom = domService.parseDocument(htmlFile);

        val signedDocument = redactableSignaturesService.signDocument(
            htmlDom,
            redactSelectors = xpathElems
        )


        temporaryFilesRepository.writeToTempFile(
            "${htmlFile.nameWithoutExtension}_signed.html",
            deleteAutomatically = false
        ) {
            domService.writeDocumentToStream(signedDocument, it)
        }

        domService.hasAllXPathElements(signedDocument, xpathElems)
            .also { assertTrue(it) }


        redactableSignaturesService.verifyDocument(signedDocument)
            .also { assertTrue(it) }

        val redactedDocument = redactableSignaturesService.redactDocument(
            signedDocument,
            redactSelectors = xpathElems
        )

        temporaryFilesRepository.writeToTempFile(
            "${htmlFile.nameWithoutExtension}_redacted.html",
            deleteAutomatically = false
        ) {
            domService.writeDocumentToStream(redactedDocument, it)
        }

        domService.hasSomeXPathElements(redactedDocument, xpathElems)
        .also { assertFalse(it) }


        redactableSignaturesService.verifyDocument(redactedDocument)
        .also { assertTrue(it) }
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFilesToRedact"])
    fun redactXPathPdf(fileName: String, xpathElems: List<String>) {

        val pdfFile = PDFFileWrapper(
            getTestFile(fileName)
        )

        val htmlDom = fileConversionService
            .generateHTMLFromPDFDoc(pdfFile)

        temporaryFilesRepository.writeToTempFile(
            "$fileName.html",
            deleteAutomatically = false
        ) {
            domService.writeDocumentToStream(htmlDom, it)
        }

        val signedDocument = redactableSignaturesService.signDocument(
            htmlDom,
            redactSelectors = xpathElems
        )
    }
}
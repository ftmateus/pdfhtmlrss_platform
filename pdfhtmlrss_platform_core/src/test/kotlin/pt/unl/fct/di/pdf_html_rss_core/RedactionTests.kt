package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.File
import kotlin.math.sign

@SpringBootTest
class RedactionTests {

    @Autowired
    lateinit var temporaryFolder : File;

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

        writeDataToTempFile(temporaryFolder, "${htmlFile.nameWithoutExtension}_signed.html") {
            domService.writeDocumentToStream(signedDocument, it)
        }

        redactableSignaturesService.verifyDocument(signedDocument)
            .also { assertTrue(it) }

        val redactedDocument = redactableSignaturesService.redactDocument(
            signedDocument,
            redactSelectors = xpathElems
        )

        writeDataToTempFile(temporaryFolder, "${htmlFile.nameWithoutExtension}_redacted.html") {
            domService.writeDocumentToStream(redactedDocument, it)
        }

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

        writeDataToTempFile(temporaryFolder, "$fileName.html") {
            domService.writeDocumentToStream(htmlDom, it)
        }

        val signedDocument = redactableSignaturesService.signDocument(
            htmlDom,
            redactSelectors = xpathElems
        )
    }
}
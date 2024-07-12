package pt.unl.fct.di.pdf_html_rss_core

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.FileConversionService
import pt.unl.fct.di.pdf_html_rss_core.services.RedactableSignaturesService
import java.io.File
import java.io.FileInputStream
import java.security.Security

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class PDFHTMLRSSApplicationTests {

	val temporaryFolder = createTemporaryTestFolder();

	@Autowired
	lateinit var fileConversionService: FileConversionService;

	@Autowired
	lateinit var redactableSignaturesService: RedactableSignaturesService;

	@Autowired
	lateinit var domService: DOMService;

	companion object {
		@BeforeAll
		@JvmStatic
		fun before() {
			val provider = WPProvider();
//			Security.addProvider(provider);
			Security.insertProviderAt(provider, 1)
		}
	}

	@Disabled
	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
	fun conversionIntegrityTest(pdfFile : PDFFileWrapper)
	{
		val domDocBytes = fileConversionService.generateHTMLFromPDF(pdfFile);

		val pdfBytes = fileConversionService.generatePDFFromHTML(domDocBytes);

		val domDocReconversionBytes = fileConversionService.generateHTMLFromPDFLinux(pdfBytes)

		writeDataToTempFile(pdfFile.getData(), temporaryFolder, "${pdfFile.name}.pdf")

		writeDataToTempFile(domDocBytes, temporaryFolder, "${pdfFile.name}1.html")

		writeDataToTempFile(domDocReconversionBytes, temporaryFolder, "${pdfFile.name}2.html")

		assertEquals(domDocBytes, domDocReconversionBytes)

	}

	@Test
	fun test2() {
		val file = getTestFile("simple.html")

		val document  = FileInputStream(file).use {
			domService.parseDocument(it)
		}

		val redactedDoc = redactableSignaturesService.signAndRedactDocument(
			document,
			redactSelectors = listOf(
				"#xpointer(id('redact'))",
//				"#xpointer(id('image'))"
			)
		);

		domService.printDocument(redactedDoc)
		val redactedDocFile = File("${temporaryFolder.path}/${file.nameWithoutExtension}_signed.${file.extension}");
		domService.writeDocumentToFile(redactedDoc, redactedDocFile)

		assertTrue(redactableSignaturesService.verifyDocument(redactedDoc));

		//TODO
//		pdfConversionService.generatePDFFromHTML(redactedDocFile);
//		pdfConversionService.generatePDFFromHTML(file, "${temporaryFolder.path}/${file.nameWithoutExtension}.${file.extension}.pdf");
	}

	@Test fun test3() {
		val file = getTestFile("simple.html")

		val document  = FileInputStream(file).use {
			domService.parseDocument(it)
		}

		val signedDoc = redactableSignaturesService.signDocument(
			document,
			redactSelectors = listOf(
				"#xpointer(id('redact'))",
				"#xpointer(id('image'))"
			)
		);

		val redactedDoc = redactableSignaturesService.redactDocument(
			signedDoc,
			redactSelectors = listOf(
				"#xpointer(id('redact'))",
			)
		)

		domService.printDocument(redactedDoc)
		val redactedDocFile = File("${temporaryFolder.path}/${file.nameWithoutExtension}_signed.${file.extension}");
		domService.writeDocumentToFile(redactedDoc, redactedDocFile)

		assertTrue(redactableSignaturesService.verifyDocument(redactedDoc));
	}
}
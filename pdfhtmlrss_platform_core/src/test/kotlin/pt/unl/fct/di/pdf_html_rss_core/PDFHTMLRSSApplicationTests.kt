package pt.unl.fct.di.pdf_html_rss_core


import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.services.DOMService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import pt.unl.fct.di.pdf_html_rss_core.services.RedactableSignaturesService
import java.io.File
import java.io.FileInputStream
import java.security.Security

//import org.junit.jupiter.api.io.TempDir;


@SpringBootTest
class PDFHTMLRSSApplicationTests {

//	@TempDir()
//	lateinit var tempDir : File;

	val temporaryFolder = let {
		val systemTmpDirPath = System.getProperty("java.io.tmpdir");
		val tmpDirPath = "$systemTmpDirPath\\pdfrss";
		
		File(tmpDirPath).apply {
			this.mkdir();
		}
	}


	@Autowired
	lateinit var pdfConversionService: PDFConversionService;

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

	@ParameterizedTest
	//TODO more html files
	@ValueSource(strings = [
		"src/test/resources/simple.html"
	])
	fun generatePDFFromHTMLTest(htmlFilePath : String) {
		val htmlFile = File(htmlFilePath)
		assumeTrue(htmlFile.exists())
		val pdfFile = File("${temporaryFolder.path}/${htmlFile.name}.pdf");

		pdfConversionService.generatePDFFromHTML(htmlFile, pdfFile.path);

		assertTrue(pdfFile.exists())
		assertTrue(pdfFile.length() > 0)
	}

	@ParameterizedTest
	//TODO
	@ValueSource(strings = [
		"src/test/resources/QS2324-assignment1-v1.0.pdf",
		"D:\\Francisco\\Downloads\\sibsforwardpaymentsolutionssa_fr_M2023-2410.pdf",
		"D:\\Francisco\\Downloads\\BoardingPass.pdf",
		"D:\\Francisco\\Downloads\\Declaracao 99_IRS.pdf",
		"D:\\Francisco\\Downloads\\Profile.pdf"
	])
	fun generateHTMLFromPDFTest(pdfFilePath : String) {
		val pdfFile = File(pdfFilePath)
		assumeTrue(pdfFile.exists());

		val htmlFile = File("${temporaryFolder.path}/${pdfFile.name}.html");

		pdfConversionService.generateHTMLFromPDF(
			pdfFilePath,
			htmlFile.absolutePath
		)

		assertTrue(htmlFile.exists())
		assertTrue(htmlFile.length() > 0)

		assertDoesNotThrow {
			val doc = domService.parseDocument(FileInputStream(htmlFile))
		}
	}

	@Test
	fun test2() {
		val file = File("src/test/resources/simple.html")

		val document = domService.parseDocument(FileInputStream(file))

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

		pdfConversionService.generatePDFFromHTML(redactedDocFile);
		pdfConversionService.generatePDFFromHTML(file, "${temporaryFolder.path}/${file.nameWithoutExtension}.${file.extension}.pdf");

	}

	@Test fun test3() {
		val file = File("src/test/resources/simple.html")

		val document = domService.parseDocument(FileInputStream(file))

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
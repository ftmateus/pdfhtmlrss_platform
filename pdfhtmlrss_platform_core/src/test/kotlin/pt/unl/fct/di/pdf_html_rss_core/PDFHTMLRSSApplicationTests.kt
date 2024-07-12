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
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.File
import java.io.FileInputStream
import java.security.Security

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class PDFHTMLRSSApplicationTests {

	@Autowired
	private lateinit var compressionService: CompressionService

	@Autowired
	private lateinit var securityService: SecurityService
	val temporaryFolder = createTemporaryTestFolder();

	@Autowired
	lateinit var fileConversionService: FileConversionService;

	@Autowired
	lateinit var redactableSignaturesService: XHTMLRedactableSignatureService;

	@Autowired
	lateinit var domService: DOMService;

	@Autowired
	lateinit var pdfManipulationService : PDFManipulationService;

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

	@Test
	fun sha256Test() {
		val file = getTestFile("simple.html")
		val fileData = FileInputStream(file).use {it.readBytes()}

		val hash = securityService.toSha256(fileData)
		assertTrue(securityService.verifySha256(fileData, hash))
	}

	@Test
	fun xPointerTest() {
		val pdfFile = PDFFileWrapper(
			getTestFile("invoice_example.pdf")
		);

		val htmlData = fileConversionService.generateHTMLFromPDF(pdfFile);

		val htmlDom = htmlData.inputStream().use {
			domService.parseDocument(it);
		}

		val signedDocument = redactableSignaturesService.signDocument(htmlDom,
			redactSelectors = listOf(
				"/html/body/div/a/p[1]"
			)
		);
	}

	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
	fun integrationTest(pdfFile : PDFFileWrapper) {
		val htmlData = fileConversionService.generateHTMLFromPDF(pdfFile);

		val htmlDom = htmlData.inputStream().use {
		 	domService.parseDocument(it);
		}

		val signedDocument = redactableSignaturesService.signAndRedactDocument(htmlDom);

		val signedDocumentData = domService.convertDomDocumentToByteArray(signedDocument);

		val compressedSignedDoc = signedDocumentData.inputStream().use {
			 compressionService.compressGZip(it);
		}

		val pdfNew = PDFFileWrapper(pdfFile.name, pdfFile.getData());

		val pdf = pdfManipulationService.addAttachmentsToPdf(pdfNew, mapOf(
			"SigTest.html.gz" to compressedSignedDoc.inputStream()
		))

		writeDataToTempFile(pdf, temporaryFolder, "${pdfFile.name}.pdf")
	}
}
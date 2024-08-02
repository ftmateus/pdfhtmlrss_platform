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
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.checkSha256WithLinux
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.*
import java.io.File
import java.io.FileInputStream
import java.security.Security
import kotlin.math.sign

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class PDFHTMLRSSApplicationTests {

	@Autowired
	private lateinit var compressionService: CompressionService

	@Autowired
	private lateinit var securityService: SecurityService

	@Autowired
	lateinit var temporaryFilesService: TemporaryFilesService

	@Autowired
	lateinit var fileConversionService: FileConversionService;

	@Autowired
	lateinit var redactableSignaturesService: XHTMLRedactableSignatureService;

	@Autowired
	lateinit var domService: DOMService;

	@Autowired
	lateinit var pdfManipulationService : PDFManipulationService;

//	companion object {
//		@BeforeAll
//		@JvmStatic
//		fun before() {
//			val provider = WPProvider();
////			Security.addProvider(provider);
//			Security.insertProviderAt(provider, 1)
//		}
//	}

	@Disabled
	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
	fun conversionIntegrityTest(pdfFile : PDFFileWrapper)
	{
		val domDocBytes = fileConversionService.generateHTMLFromPDF(pdfFile);

		val pdfBytes = fileConversionService.generatePDFFromHTML(domDocBytes);

		val domDocReconversionBytes = fileConversionService.generateHTMLFromPDF(pdfBytes)

		temporaryFilesService.writeToTempFile(
			pdfFile.getInputStream(),
			"${pdfFile.name}.pdf",
			deleteAutomatically = false
		)

		temporaryFilesService.writeToTempFile(
			domDocBytes.inputStream(),
			"${pdfFile.name}1.html",
			deleteAutomatically = false
		)

		temporaryFilesService.writeToTempFile(
			domDocReconversionBytes.inputStream(),
			"${pdfFile.name}2.html",
			deleteAutomatically = false
		)

		assertEquals(domDocBytes, domDocReconversionBytes)

	}

	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allTestFiles"])
	fun sha256Test(file : File) {
		val fileData = FileInputStream(file).use {it.readBytes()}

		val hash = securityService.toSha256(fileData)
		assertTrue(securityService.verifySha256(fileData, hash))
		checkSha256WithLinux(hash, file)
	}

	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#smallPdfTestFiles"])
	fun verifySeparateTest(pdfFile : PDFFileWrapper) {
		val htmlDom = fileConversionService.generateHTMLFromPDFDoc(pdfFile);

		val signatureDom = redactableSignaturesService.signAndRedactDocument(htmlDom);

		assertTrue(redactableSignaturesService.verifyDocument(htmlDom, signatureDom))
	}

	@ParameterizedTest
	@MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#smallPdfTestFiles"])
	fun pdfSignWithRedactableSignature(pdfFile : PDFFileWrapper) {
		val signedPdf = pdfManipulationService.signPdfFileRedactableSignature(pdfFile)

		temporaryFilesService.writeToTempFile(
			signedPdf.getInputStream(),
			"${signedPdf.name}.pdf",
			deleteAutomatically = false
		)

		pdfManipulationService.verifyPdfFileRedactableSignature(signedPdf)
			.also { assertTrue(it) }
	}
}
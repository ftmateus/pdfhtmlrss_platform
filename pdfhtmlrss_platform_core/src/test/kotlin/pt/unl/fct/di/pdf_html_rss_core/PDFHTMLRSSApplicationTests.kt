package pt.unl.fct.di.pdf_html_rss_core


import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.w3c.dom.Document
import org.w3c.dom.Element
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import pt.unl.fct.di.pdf_html_rss_core.services.RedactableSignaturesService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

//import org.junit.jupiter.api.io.TempDir;


@SpringBootTest
class PDFHTMLRSSApplicationTests {

//	@TempDir()
//	lateinit var tempDir : File;


	@Autowired
	lateinit var pdfConversionService: PDFConversionService;

	@Autowired
	lateinit var redactableSignaturesService: RedactableSignaturesService;

	companion object {
		@BeforeAll
		@JvmStatic
		fun before() {
			val provider = WPProvider();
//			Security.addProvider(provider);
			Security.insertProviderAt(provider, 1)

		}
	}

	@Test fun generateHTMLFromPDFTest1() {
		pdfConversionService.generatePDFFromHTML(File("src/test/resources/simple.html"));
	}

	@Test fun generateHTMLFromPDFTest() {
		val dstFile = File("testfiles/QS2324-assignment1-v1.0.pdf.html");

		pdfConversionService.generateHTMLFromPDF(
			"src/test/resources/QS2324-assignment1-v1.0.pdf",
			dstFile.absolutePath
		)

		assertTrue(dstFile.exists())
		assertTrue(dstFile.length() > 0)

		val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
		//https://github.com/qzind/tray/commit/c04b510515246954a5a26475ae46434b7f127437
		dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()

		assertDoesNotThrow { val doc: Document = dBuilder.parse(dstFile) }

//		val sig = RedactableXMLSignature.getInstance("XMLPSRSSwithPSA");
	}

	@Test fun generatePDFFromHTMLTest() {
		pdfConversionService.generatePDFFromHTML(File("testfiles/clip.html"));
	}

	@Test fun test2() {
		val file = File("src/test/resources/simple.html")

		val document = let {
			DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(file)
		}

		val redactedDoc = redactableSignaturesService.signAndRedactDocument(
			document,
			redactSelectors = listOf(
				"#xpointer(id('redact'))",
//				"#xpointer(id('image'))"
			)
		);

		printDocument(redactedDoc)
		val redactedDocFile = File("testfiles/${file.nameWithoutExtension}_signed.${file.extension}");
		writeDocumentToFile(redactedDoc, redactedDocFile)

		assertTrue(redactableSignaturesService.verifyDocument(redactedDoc));

		pdfConversionService.generatePDFFromHTML(redactedDocFile);
		pdfConversionService.generatePDFFromHTML(file, "testfiles/${file.nameWithoutExtension}.${file.extension}.pdf");

	}

	@Test fun test3() {
		val file = File("src/test/resources/simple.html")

		val document = let {
			DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(file)
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

		printDocument(redactedDoc)
		val redactedDocFile = File("testfiles/${file.nameWithoutExtension}_signed.${file.extension}");
		writeDocumentToFile(redactedDoc, redactedDocFile)

		assertTrue(redactableSignaturesService.verifyDocument(redactedDoc));
	}

	@Throws(TransformerException::class)
	protected fun printDocument(document: Document?) {
		writeDocumentToStream(document, System.out)
	}

	@Throws(TransformerException::class)
	protected fun writeDocumentToFile(document: Document?, file : File) {
		writeDocumentToStream(document, FileOutputStream(file))
	}

	protected fun writeDocumentToStream(document: Document?, io : OutputStream) {
		val tf = TransformerFactory.newInstance()
		val trans = tf.newTransformer()
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
		trans.transform(DOMSource(document), StreamResult(io))
	}

}
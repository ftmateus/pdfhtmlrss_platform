package pt.unl.fct.di.pdf_html_rss_core


import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.io.File
import java.io.FileInputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


@SpringBootTest
class PDFHTMLRSSApplicationTests {

//	val keyPair = KeyPair(
//		PSRSSPublicKey(BigInteger("7249349928048807500024891411067629370056303429447255270046802991880425543412906735607605108373982421012500888307062421310001762155422489671132976679912849")),
//		PSRSSPrivateKey(BigInteger("7249349928048807500024891411067629370056303429447255270046802991880425543412734960638035580933850038621738468566657503090109097536944629352405060890801636"))
//	);

	val keyPair: KeyPair = run {
		val keyGen = KeyPairGenerator.getInstance("GLRSSwithRSAandBPA")
		keyGen.initialize(512)
		keyGen.generateKeyPair()
	};

	@Autowired
	var pdfConversionService: PDFConversionService? = null;

	companion object {

		@BeforeAll
		@JvmStatic
		fun before() {
			val provider = WPProvider();
//			Security.addProvider(provider);
			Security.insertProviderAt(provider, 1)

		}
	}

	@Test fun generateHTMLFromPDFTest() {
		val dstFile = File("testfiles/QS2324-assignment1-v1.0.pdf.html");

		pdfConversionService?.generateHTMLFromPDF(
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
		pdfConversionService?.generatePDFFromHTML("testfiles/clip.html");
	}

	@Test fun test2() {
		val filename = "src/test/resources/simple.html"
//		val filename = "src/test/resources/vehicles.xml"

		val sig = RedactableXMLSignature.getInstance("GLRSSwithRSAandBPA");

		sig.initSign(keyPair);

		sig.setDocument(FileInputStream(filename));

//		sig.addSignSelector("#xpointer(/2/1)", false)
		sig.addSignSelector("#xpointer(id('a1'))", false)
	}

}
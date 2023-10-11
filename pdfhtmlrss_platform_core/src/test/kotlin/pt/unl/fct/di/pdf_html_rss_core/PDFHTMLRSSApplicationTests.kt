package pt.unl.fct.di.pdf_html_rss_core


import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.psrss.PSRSSPrivateKey
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.psrss.PSRSSPublicKey
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.io.FileInputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.Security
import javax.xml.parsers.DocumentBuilderFactory


@SpringBootTest
class PDFHTMLRSSApplicationTests {

	val keyPair = KeyPair(
		PSRSSPublicKey(BigInteger("7249349928048807500024891411067629370056303429447255270046802991880425543412906735607605108373982421012500888307062421310001762155422489671132976679912849")),
		PSRSSPrivateKey(BigInteger("7249349928048807500024891411067629370056303429447255270046802991880425543412734960638035580933850038621738468566657503090109097536944629352405060890801636"))
	);

	@Autowired
	var pdfConversionService: PDFConversionService? = null;

	companion object {
		@BeforeAll
		fun before() {
			Security.addProvider(WPProvider());
		}
	}

	@Test fun test1() {
		pdfConversionService?.generateHTMLFromPDF("QS2324-assignment1-v1.0.pdf")

		val sig = RedactableXMLSignature.getInstance("XMLPSRSSwithPSA");
	}

	@Test fun test2() {
		//val filename = "Horários Resultado _ CP - Comboios de Portugal.pdf"
		val filename = "QS2324-assignment1-v1.0.pdf.html"
		//val filename = "test1.xml"
		//generateHTMLFromPDF(filename)

		val sig = RedactableXMLSignature.getInstance("XMLPSRSSwithPSA");

		sig.initSign(keyPair);
		val documentBuilderFactory = DocumentBuilderFactory.newInstance()
		documentBuilderFactory.isNamespaceAware = false
		documentBuilderFactory.isValidating = false
		documentBuilderFactory.isIgnoringElementContentWhitespace = true

		val documentBuilder = documentBuilderFactory.newDocumentBuilder()

		sig.setDocument(documentBuilder.parse(FileInputStream(filename)));
	}

}

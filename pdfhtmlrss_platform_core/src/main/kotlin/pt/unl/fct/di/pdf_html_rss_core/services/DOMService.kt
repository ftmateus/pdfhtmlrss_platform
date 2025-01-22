package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.Dereferencer
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignatureException
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Node
import pt.unl.fct.di.pdf_html_rss_core.components.DomEntityResolver
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

@Service
class DOMService {

    companion object {
        val HTML_META_DATE_XPATH = let {
            val xPath = XPathFactory.newInstance().newXPath()

            xPath.compile("/html/head/meta[@name='date']")
        }
    }

    @Autowired
    lateinit var domEntityResolver: DomEntityResolver;

    //    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    @Autowired
    lateinit var documentBuilderFactoryDefault: DocumentBuilderFactory;

    @Autowired
    lateinit var documentBuilderFactoryFileConversion: DocumentBuilderFactory;

    private fun createDocumentBuilder(fileConversion : Boolean = false): DocumentBuilder {
        val factory = if (fileConversion) documentBuilderFactoryFileConversion
        else documentBuilderFactoryDefault

        return factory.newDocumentBuilder()
            .also {
                it.setEntityResolver(domEntityResolver)
            }
    }

    fun hasAllXPathElements(document: Document, xPathElems : List<String>): Boolean {
        return xPathElems.stream()
            .allMatch { xPathElementExists(document, it) }
    }

    fun hasSomeXPathElements(document: Document, xPathElems : List<String>): Boolean {
        return xPathElems.stream()
            .anyMatch { xPathElementExists(document, it) }
    }

    fun xPathElementExists(document: Document, xPathElem : String) : Boolean {
        return try {
            Dereferencer.dereference(xPathElem, document) != null
        } catch (e : RedactableXMLSignatureException) {
            false
        }
    }

    @Deprecated("Use input stream implementation")
    fun convertDomDocumentToByteArray(document : Document) : ByteArray
    {
        ByteArrayOutputStream().use {
            writeDocumentToStream(document, it);
            return it.toByteArray();
        }
    }

    @Throws(TransformerException::class)
    fun printDocument(document: Document) {
        writeDocumentToStream(document, System.out, true)
    }

    @Throws(TransformerException::class)
    fun writeDocumentToFile(document: Document, file : File) {
        FileOutputStream(file).use {
            writeDocumentToStream(document, it)
        }
    }

    fun writeDocumentToStream(document: Document, out : OutputStream, indentation : Boolean = false) {
        val tf = TransformerFactory.newInstance()
        val trans = tf.newTransformer()

        trans.setOutputProperty(OutputKeys.METHOD, "xml")
//        trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "rss.dtd");

        if(indentation) {
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
        } else {
            trans.setOutputProperty(OutputKeys.INDENT, "no");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        }
        trans.transform(DOMSource(document), StreamResult(out))
    }

    fun parseDocument(inputStream : InputStream, fileConversion: Boolean = false) : Document {
        val documentBuilder = createDocumentBuilder(fileConversion);

        return documentBuilder.parse(inputStream);
    }

    fun parseDocument(file : File) : Document {
//        return W3CDom()
//            .fromJsoup(Jsoup.parse(file));
        file.inputStream().use {
            return parseDocument(it)
        }
    }

    fun removeDateMetaHtmlElement(domDoc : Document) {

        val metaElem = HTML_META_DATE_XPATH.evaluate(domDoc, XPathConstants.NODE) as Node?

        metaElem?.parentNode?.removeChild(metaElem)
    }
}
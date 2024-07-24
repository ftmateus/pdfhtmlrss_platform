package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
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

    //    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    @Autowired
    lateinit var documentBuilder: DocumentBuilder;

    fun convertDomDocumentToByteArray(document : Document) : ByteArray
    {
        ByteArrayOutputStream().use {
            writeDocumentToStream(document, it);
            return it.toByteArray();
        }
    }

    @Throws(TransformerException::class)
    fun printDocument(document: Document) {
        writeDocumentToStream(document, System.out)
    }

    @Throws(TransformerException::class)
    fun writeDocumentToFile(document: Document, file : File) {
        FileOutputStream(file).use {
            writeDocumentToStream(document, it)
        }
    }

    fun writeDocumentToStream(document: Document, out : OutputStream) {
        val tf = TransformerFactory.newInstance()
        val trans = tf.newTransformer()
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
        trans.transform(DOMSource(document), StreamResult(out))
    }

    fun parseDocument(inputStream : InputStream) : Document {
        return documentBuilder.parse(inputStream);
    }

    fun parseDocument(file : File) : Document {
        FileInputStream(file).use {
            return parseDocument(it)
        }
    }

    fun removeDateMetaHtmlElement(domDoc : Document) {

        val metaElem = HTML_META_DATE_XPATH.evaluate(domDoc, XPathConstants.NODE) as Node?

        metaElem?.parentNode?.removeChild(metaElem)
    }
}
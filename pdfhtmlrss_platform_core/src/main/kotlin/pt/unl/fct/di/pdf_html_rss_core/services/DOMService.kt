package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.stereotype.Service
import org.w3c.dom.Document
import java.io.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Service
class DOMService {

//    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    val documentBuilder: DocumentBuilder = let {
        val dbFactory = DocumentBuilderFactory
            .newInstance();

        //https://github.com/qzind/tray/commit/c04b510515246954a5a26475ae46434b7f127437
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        dbFactory.newDocumentBuilder();
    }

    fun convertDomDocumentToByteArray(document : Document) : ByteArray
    {
        val out = ByteArrayOutputStream();
        writeDocumentToStream(document, out);

        return out.toByteArray();
    }

    @Throws(TransformerException::class)
    fun printDocument(document: Document) {
        writeDocumentToStream(document, System.out)
    }

    @Throws(TransformerException::class)
    fun writeDocumentToFile(document: Document, file : File) {
        writeDocumentToStream(document, FileOutputStream(file))
    }

    fun writeDocumentToStream(document: Document, io : OutputStream) {
        val tf = TransformerFactory.newInstance()
        val trans = tf.newTransformer()
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
        trans.transform(DOMSource(document), StreamResult(io))
    }

    fun parseDocument(inputStream : InputStream) : Document {
        return documentBuilder.parse(inputStream);
    }

    fun parseDocument(file : File) : Document {
        return parseDocument(FileInputStream(file))
    }
}
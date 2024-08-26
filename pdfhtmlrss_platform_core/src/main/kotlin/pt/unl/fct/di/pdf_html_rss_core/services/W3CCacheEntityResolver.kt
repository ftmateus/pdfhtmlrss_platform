package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.net.URL

@Component
class W3CCacheEntityResolver : EntityResolver {

    @Autowired
    lateinit var temporaryFolder : File;

    @Throws(SAXException::class, IOException::class)
    override fun resolveEntity(publicId: String?, systemId: String): InputSource? {
        var systemIdUrl : URL? = URL(systemId)

        if (systemIdUrl?.protocol == "file") {
            val fileName = File(systemIdUrl.file).name
            systemIdUrl = mapW3CFilesToHttpURL(fileName)
            if(systemIdUrl == null) return null;
        } else if (systemIdUrl?.host != "www.w3.org") return null;

        val cachedFile = File(temporaryFolder.path, "${systemIdUrl.host}/${systemIdUrl.path}")

        if (!cachedFile.exists()) {
            createW3CCacheFile(cachedFile, systemIdUrl);
        }

        return InputSource(cachedFile.inputStream())
    }

    fun mapW3CFilesToHttpURL(fileName : String) : URL? {
        val url = when(fileName) {
            "xhtml-datatypes-1.mod", "xhtml-events-1.mod", "xhtml-qname-1.mod",
            "xhtml-attribs-1.mod", "xhtml-charent-1.mod", "xhtml-inlstruct-1.mod",
            "xhtml-inlphras-1.mod", "xhtml-ruby-1.mod", "xhtml-script-1.mod",
            "xhtml-meta-1.mod", "xhtml-blkstruct-1.mod", "xhtml-blkphras-1.mod",
            "xhtml-inlpres-1.mod", "xhtml-blkpres-1.mod"
             -> "https://www.w3.org/MarkUp/DTD/${fileName}"
            "xhtml-lat1.ent", "xhtml-symbol.ent", "xhtml-special.ent"
            -> "http://www.w3.org/TR/xhtml1/DTD/${fileName}"
            else -> null
        }

        return if (url != null) URL(url) else null
    }

    private fun createW3CCacheFile(w3cLocalFile : File, w3cUrl: URL) {
        w3cLocalFile.parentFile.mkdirs()
        w3cLocalFile.createNewFile()
        w3cUrl.openStream().use { inputStream ->
            w3cLocalFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
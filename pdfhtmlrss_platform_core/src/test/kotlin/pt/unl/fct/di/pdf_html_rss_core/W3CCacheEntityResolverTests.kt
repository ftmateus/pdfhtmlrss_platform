package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Timeout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.services.W3CCacheEntityResolver
import java.io.File
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

@SpringBootTest
class W3CCacheEntityResolverTests {

    @Autowired
    lateinit var w3CCacheEntityResolver: W3CCacheEntityResolver;

    @Autowired
    lateinit var documentBuilderFactory: DocumentBuilderFactory;

    @Autowired
    lateinit var temporaryFolder : File;


    @Disabled
    @RepeatedTest(10)
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    fun testResolver() {
        val file = getTestFile("simple_signed.html")

        val dbBuilder = documentBuilderFactory.newDocumentBuilder()
            .also {
                it.setEntityResolver(w3CCacheEntityResolver)
            }

        dbBuilder.parse(file)
    }

}
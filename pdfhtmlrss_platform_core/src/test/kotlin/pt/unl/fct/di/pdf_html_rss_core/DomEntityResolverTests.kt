package pt.unl.fct.di.pdf_html_rss_core

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.components.DomEntityResolver
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

@SpringBootTest
class DomEntityResolverTests {

    @Autowired
    lateinit var domEntityResolver: DomEntityResolver;

    @Autowired
    lateinit var documentBuilderFactoryDefault: DocumentBuilderFactory;

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository


    @Test
    fun testResolver() {
        // TODO assume that internet connection is available

        val file = getTestFile("simple_signed.html")

        temporaryFilesRepository.getTempFile("www.w3.org")
            ?.deleteRecursively()

        val dbBuilder = documentBuilderFactoryDefault.newDocumentBuilder()
            .also {
                it.setEntityResolver(domEntityResolver)
            }

        val durationWithoutCache = measureTimeMillis {
            runBlocking {
                dbBuilder.parse(file)
            }
        }

        println("Duration without cache : $durationWithoutCache")

        assertNotNull(temporaryFilesRepository.getTempFile("www.w3.org"))

        //at least 10 times faster
        val expectedDurationWithCache = (durationWithoutCache * 0.1).toLong()

        val actualDurationWithCache = measureTimeMillis {
            runBlocking {
                withTimeout(expectedDurationWithCache) {
                    dbBuilder.parse(file)
                }
            }
        }

        println("Duration with cache : $actualDurationWithCache")
    }

}
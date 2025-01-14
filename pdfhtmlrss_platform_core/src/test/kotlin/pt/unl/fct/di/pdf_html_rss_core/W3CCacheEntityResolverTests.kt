package pt.unl.fct.di.pdf_html_rss_core

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.getTestFile
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.components.W3CCacheEntityResolver
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.measureTimeMillis

@SpringBootTest
class W3CCacheEntityResolverTests {

    @Autowired
    lateinit var w3CCacheEntityResolver: W3CCacheEntityResolver;

    @Autowired
    lateinit var documentBuilderFactory: DocumentBuilderFactory;

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository


    @Test
    fun testResolver() {
        // TODO assume that internet connection is available

        val file = getTestFile("simple_signed.html")

        temporaryFilesRepository.getTempFile("www.w3.org")
            ?.deleteRecursively()

        val dbBuilder = documentBuilderFactory.newDocumentBuilder()
            .also {
                it.setEntityResolver(w3CCacheEntityResolver)
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
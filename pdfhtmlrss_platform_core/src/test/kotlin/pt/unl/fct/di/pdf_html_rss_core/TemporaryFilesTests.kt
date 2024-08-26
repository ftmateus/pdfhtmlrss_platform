package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.File

@SpringBootTest
class TemporaryFilesTests {
    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    lateinit var securityService: SecurityService

    @ParameterizedTest
    @MethodSource("pt.unl.fct.di.pdf_html_rss_core.TestUtils#allTestFiles")
    fun writeRandomNameTemporaryFileTest(testFile : File) {
        val tempFile = temporaryFilesRepository.writeToTempFile(
            testFile.inputStream(),
            deleteAutomatically = true
        )

        temporaryFilesRepository.getTempFile(tempFile.name)
            .also {
                assertNotNull(it)
                assertNotEquals(tempFile.name, testFile.name)
            }

        val testFileSha256 = testFile.inputStream().use {
            securityService.toSha256(it.readBytes())
        }
        val tempFileSha256 = tempFile.inputStream().use {
            securityService.toSha256(it.readBytes())
        }

        assertEquals(testFileSha256, tempFileSha256)
    }

    @ParameterizedTest
    @MethodSource("pt.unl.fct.di.pdf_html_rss_core.TestUtils#allTestFiles")
    fun writeCustomNameTemporaryFileTest(testFile : File) {
        val tempFile = temporaryFilesRepository.writeToTempFile(
            testFile.inputStream(),
            name = testFile.name,
            deleteAutomatically = true
        )

        temporaryFilesRepository.getTempFile(tempFile.name)
            .also {
                assertNotNull(it)
                assertEquals(tempFile.name, testFile.name)
            }

        val testFileSha256 = testFile.inputStream().use {
            securityService.toSha256(it)
        }
        val tempFileSha256 = tempFile.inputStream().use {
            securityService.toSha256(it)
        }

        assertEquals(testFileSha256, tempFileSha256)
    }

    //TODO test automatic deletion with custom TTLs
}
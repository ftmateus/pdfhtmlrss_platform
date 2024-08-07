package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.CompressionService
import pt.unl.fct.di.pdf_html_rss_core.services.TemporaryFilesService
import java.io.File

@SpringBootTest
class CompressionTests {

    @Autowired
    lateinit var temporaryFilesService: TemporaryFilesService

    @Autowired
    lateinit var compressionService: CompressionService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFiles"])
    fun compressHTMLFilesGZip(htmlFile : File) {
        _genericGZipCompressionTest(htmlFile);
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#allPdfTestFiles"])
    fun compressPDFFilesGZip(pdfFile : PDFFileWrapper) {
        _genericGZipCompressionTest(pdfFile.resource.file);
    }

    fun _genericGZipCompressionTest(file : File) {
        val originalData = file.readBytes();

        val compressedData = originalData.inputStream().use {
            compressionService.compressGZip(it)
        }

        temporaryFilesService.writeToTempFile(
            compressedData.inputStream(),
            "${file.name}.gzip",
            deleteAutomatically = false
        )

        val compressionRatio = compressedData.size.toFloat()/originalData.size.toFloat();

        assert(compressionRatio < 1.0);

        val decompressedData = compressedData.inputStream().use {
            compressionService.decompressGZip(it);
        }

        temporaryFilesService.writeToTempFile(
            decompressedData.inputStream(),
            "${file.name}.gzip.${file.extension}",
            deleteAutomatically = false
        )

        assertEquals(originalData.size, decompressedData.size);
        assertTrue(originalData.contentEquals(decompressedData));
    }
}
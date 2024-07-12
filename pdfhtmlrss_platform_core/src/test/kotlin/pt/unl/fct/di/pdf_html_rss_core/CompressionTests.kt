package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.services.CompressionService
import java.io.File

@SpringBootTest
class CompressionTests {

    val temporaryFolder = createTemporaryTestFolder();

    @Autowired
    lateinit var compressionService: CompressionService;

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#htmlTestFiles"])
    fun compressHTMLFilesGZip(htmlFile : File) {
        _genericGZipCompressionTest(htmlFile);
    }

    @ParameterizedTest
    @MethodSource(value = ["pt.unl.fct.di.pdf_html_rss_core.TestUtils#pdfTestFiles"])
    fun compressPDFFilesGZip(pdfFile : PDFFileWrapper) {
        _genericGZipCompressionTest(pdfFile.file!!);
    }

    fun _genericGZipCompressionTest(file : File) {
        val originalData = file.readBytes();

        val compressedData = compressionService.compressGZip(file.inputStream())
        writeDataToTempFile(compressedData, temporaryFolder, "${file.name}.gzip")

        val compressionRatio = compressedData.size.toFloat()/originalData.size.toFloat();

        assert(compressionRatio < 1.0);

        val decompressedData = compressionService.decompressGZip(compressedData.inputStream());
        writeDataToTempFile(decompressedData, temporaryFolder, "${file.name}.gzip.${file.extension}")

        assertEquals(originalData.size, decompressedData.size);
        assertEquals(originalData.toList(), decompressedData.toList());
    }
}
package pt.unl.fct.di.pdf_html_rss_core

import org.bouncycastle.internal.asn1.cms.CMSObjectIdentifiers.compressedData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.createTemporaryTestFolder
import pt.unl.fct.di.pdf_html_rss_core.TestUtils.Companion.writeDataToTempFile
import pt.unl.fct.di.pdf_html_rss_core.services.CompressionService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.io.File
import java.io.FileOutputStream

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
    fun compressPDFFilesGZip(pdfFile : File) {
        _genericGZipCompressionTest(pdfFile);
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
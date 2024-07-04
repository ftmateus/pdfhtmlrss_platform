package pt.unl.fct.di.pdf_html_rss_core

import org.bouncycastle.internal.asn1.cms.CMSObjectIdentifiers.compressedData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pt.unl.fct.di.pdf_html_rss_core.services.CompressionService
import pt.unl.fct.di.pdf_html_rss_core.services.PDFConversionService
import java.io.File
import java.io.FileOutputStream

@SpringBootTest
class CompressionTests {
    val temporaryFolder = let {
        val systemTmpDirPath = System.getProperty("java.io.tmpdir");
        val tmpDirPath = "$systemTmpDirPath${File.separator}pdfrss";

        File(tmpDirPath).also { it.mkdir() }
    }

    @Autowired
    lateinit var compressionService: CompressionService;

    fun _genericGZipCompressionTest(file : File) {
        val originalData = file.readBytes();

        val compressedData = compressionService.compressGZip(file.inputStream())

        val compressionRatio = compressedData.size.toFloat()/originalData.size.toFloat();

        assert(compressionRatio < 1.0);

        val decompressedData = compressionService.decompressGZip(compressedData.inputStream());

        assertEquals(originalData.size, decompressedData.size);
        assertEquals(originalData.toList(), decompressedData.toList());
    }

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
}
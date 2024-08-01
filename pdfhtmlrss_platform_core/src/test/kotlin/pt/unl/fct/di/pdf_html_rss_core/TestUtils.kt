package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.provider.Arguments
import org.springframework.cglib.core.Block
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.stream.Stream

class TestUtils {

    companion object {

        private const val TEST_FILES_FOLDER_PATH = "./testfiles";


        private const val SMALL_PDF_SPACE_THRESHOLD : Long = 1*1024*1024;

        @JvmStatic
        fun getTestFile(fileName : String) : File {
            return File(TEST_FILES_FOLDER_PATH, fileName).also {
                assumeTrue(it.exists())
                assumeTrue(it.isFile)
            };
        }

        @JvmStatic
        fun pdfTestFiles(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "pdf" }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(PDFFileWrapper(it)) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun smallPdfTestFiles(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter {
                    it.extension == "pdf" &&
                    it.length() < SMALL_PDF_SPACE_THRESHOLD
                }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(PDFFileWrapper(it)) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun htmlTestFiles(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "html" }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun htmlTestFilesToRedact() : Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "simple.html", listOf(
                        "#xpath(/html/body/div/label[2])"
                    )
                ),
            )
        }

        @JvmStatic
        fun pdfTestFilesToRedact() : Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "sample.pdf", listOf(
                        "#xpath(/html/body/div/p[18])"
                    )
                ),
            )
        }

        @JvmStatic
        fun allTestFiles() : Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        @Deprecated("Uses a lot of memory")
        @JvmStatic
        fun writeDataToTempFile(data : ByteArray, temporaryFolder : File, fileName : String) {
            val tempFile = File(temporaryFolder, fileName);
            FileOutputStream(tempFile).use {
                it.write(data);
            }
        }

        @JvmStatic
        fun writeDataToTempFile(temporaryFolder : File, fileName : String, block : (OutputStream) -> Unit) {
            val tempFile = File(temporaryFolder, fileName);
            tempFile.outputStream().use(block)
        }

        @JvmStatic
        fun checkSha256WithLinux(expectedSha256 : String, file : File) {
            val sha256sumProcess = ProcessBuilder()
                .command("/usr/bin/sha256sum", "-c")
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start()

            sha256sumProcess.outputStream.use {
                it.write("$expectedSha256 ${file.absolutePath}".toByteArray())
            }

            sha256sumProcess.waitFor()

            assertTrue(sha256sumProcess.exitValue() == 0)
        }
    }

}
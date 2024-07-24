package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.provider.Arguments
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.File
import java.io.FileOutputStream
import java.util.stream.Stream

class TestUtils {
    companion object {

        private const val TEST_FILES_FOLDER_PATH = "./testfiles";

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
        fun htmlTestFiles(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "html" }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun allTestFiles() : Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun createTemporaryTestFolder() : File {
            val systemTmpDirPath = System.getProperty("java.io.tmpdir");
            val tmpDirPath = "$systemTmpDirPath${File.separator}pdfrss";

            return File(tmpDirPath).also {
                it.mkdir()
            }
        }

        @JvmStatic
        fun writeDataToTempFile(data : ByteArray, temporaryFolder : File, fileName : String) {
            val tempFile = File(temporaryFolder, fileName);
            FileOutputStream(tempFile).use {
                it.write(data);
            }
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
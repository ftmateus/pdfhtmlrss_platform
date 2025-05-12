package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.provider.Arguments
import pt.unl.fct.di.pdf_html_rss_core.data.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.utils.encodeAsHex
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.streams.toList

class TestUtils {

    companion object {

        private const val TEST_FILES_FOLDER_PATH = "./testfiles";

        private const val SMALL_PDF_SPACE_THRESHOLD : Long = 1*1024*1024;

        @JvmStatic
        fun getTestFile(fileName : String) : File {
            return File(TEST_FILES_FOLDER_PATH, fileName).also {
                assumeTrue(it.exists(), "$fileName does not exist")
                assumeTrue(it.isFile, "$fileName is not a valid file")
            };
        }

        @JvmStatic
        fun allPdfTestFiles(): Stream<Arguments> {
            val random = Random()
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "pdf" }
                ?.sortedBy { random.nextInt() }
//                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { Arguments.of(PDFFileWrapper(it)) }
                ?.stream() ?: Stream.empty();
        }

        //TODO create test cases
        @JvmStatic
        fun pdfTestFilesWithOnePage(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "pdf" }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { PDFFileWrapper(it) }
                ?.filter { it.numberOfPages == 1 }
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        //TODO create test cases
        @JvmStatic
        fun pdfTestFilesWithMultiplePages(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.extension == "pdf" }
                ?.sortedBy { it.totalSpace }
                ?.asReversed()
                ?.map { PDFFileWrapper(it) }
                ?.filter { it.numberOfPages > 1 }
                ?.map { Arguments.of(it) }
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
        fun largePdfTestFiles(): Stream<Arguments> {
            return File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter {
                    it.extension == "pdf" &&
                    it.length() >= SMALL_PDF_SPACE_THRESHOLD
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
                    ),
                ),
                Arguments.of(
                    "lorem_ipsum.html", IntStream.range(1, 13)
                        .mapToObj { "#xpath(/html/body/p[$it])" }
                        .toList()
                )
            )
        }

        @JvmStatic
        fun testFilesToRedact(fileExtension : String) : Stream<Arguments> {
            val random = Random()
            val pdfFilesToRedact = File(TEST_FILES_FOLDER_PATH).listFiles()
                ?.filter { it.name.endsWith("redact.txt")
                        && it.name.matches(".*.$fileExtension.*".toRegex()) }
                ?.sortedBy { random.nextInt() }
                ?.map {
                    Arguments.of(it.name.removeSuffix(".redact.txt"))
                }
                ?: emptyList()

            return pdfFilesToRedact.stream()
        }

        @JvmStatic
        fun pdfTestFilesToRedact() : Stream<Arguments> {
            return testFilesToRedact("pdf")
        }

        @JvmStatic
        fun getRedactSelectors(testFileName : String) : List<String> {
            val redactSelectorsFile = getTestFile("$testFileName.redact.txt")
            return redactSelectorsFile.inputStream().use { inputStream ->
                inputStream
                    .readAllBytes()
                    .toString(Charset.defaultCharset())
                    .split("\n")
            }
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
        fun checkSha256WithLinux(expectedSha256 : ByteArray, file : File) {
            val sha256sumProcess = ProcessBuilder()
                .command("/usr/bin/sha256sum", "-c")
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start()

            val hashAsHex = encodeAsHex(expectedSha256)

            sha256sumProcess.outputStream.use {
                it.write("$hashAsHex ${file.absolutePath}".toByteArray())
            }

            sha256sumProcess.waitFor()

            assertTrue(sha256sumProcess.exitValue() == 0)
        }
    }

}
package pt.unl.fct.di.pdf_html_rss_core

import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.util.stream.Stream

class TestUtils {
    companion object {
        @JvmStatic
        public fun pdfTestFiles(): Stream<Arguments> {
            return File("./testfiles/").listFiles()
                ?.filter { it.extension == "pdf" }
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }

        @JvmStatic
        fun htmlTestFiles(): Stream<Arguments> {
            return File("./testfiles/").listFiles()
                ?.filter { it.extension == "html" }
                ?.map { Arguments.of(it) }
                ?.stream() ?: Stream.empty();
        }
    }

}
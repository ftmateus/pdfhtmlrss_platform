package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Service
class CompressionService {

    fun decompressGZip(compressedData: InputStream) : ByteArray {
        ByteArrayOutputStream().use { baos ->
            decompressGZip(compressedData, baos)
            return baos.toByteArray().also {
                assert(it.isNotEmpty())
            };
        }
    }

    fun decompressGZip(
        compressedData: InputStream,
        fos  : OutputStream = ByteArrayOutputStream()
    ) {
        val gis = GZIPInputStream(compressedData);

        try {
            gis.copyTo(fos)
        } finally {
            compressedData.close()
            gis.close()
            fos.close()
        }
    }

    fun compressGZip(
        output : OutputStream = ByteArrayOutputStream(),
        writeToGZipOutputStream : (GZIPOutputStream) -> Unit
    ) {
        val gzipos = GZIPOutputStream(output);

        try {
            writeToGZipOutputStream(gzipos);

            gzipos.finish()
        } finally {
            output.close();
            gzipos.close()
        }

    }

    fun compressGZip(rawData : InputStream, output: OutputStream) {
        compressGZip(output = output) { gzipos ->
            rawData.use {
                it.copyTo(gzipos);
            }
        }
    }

//    @Deprecated("Consider using the I/O streams implementations")
    fun compressGZip(rawData : InputStream) : ByteArray {
        ByteArrayOutputStream().use { baos ->

            compressGZip(rawData, output = baos);

            return baos.toByteArray().also {
                assert(it.isNotEmpty())
            }
        }
    }
}
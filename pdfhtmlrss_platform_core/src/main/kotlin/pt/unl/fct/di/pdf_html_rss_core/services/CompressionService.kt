package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Service
class CompressionService {

    fun decompressGZip(compressedData: InputStream): ByteArray {
        val gis = GZIPInputStream(compressedData);
        val fos = ByteArrayOutputStream();

        try {
            val buffer = ByteArray(8*1024)
            var len: Int
            while ((gis.read(buffer).also { len = it }) != -1) {
                fos.write(buffer, 0, len)
            }
            return fos.toByteArray().also {
                assert(it.isNotEmpty())
            };
        } finally {
            compressedData.close()
            gis.close()
            fos.close()
        }
    }

    fun compressGZip(writeToGZipOutputStream : (OutputStream) -> Unit) : ByteArray {
        val out = ByteArrayOutputStream();
        val gzipos = GZIPOutputStream(out);

        try {
            writeToGZipOutputStream(gzipos);

            gzipos.finish()

            return out.toByteArray().also {
                assert(it.isNotEmpty())
            }
        } finally {
            out.close();
            gzipos.close()
        }
    }

    fun compressGZip(rawData : InputStream): ByteArray {
        return compressGZip { gzipos ->
            rawData.use {
                it.copyTo(gzipos);
            }
        }
    }
}
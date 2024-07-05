package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Service
class CompressionService {
    fun decompressGZip(compressedData: InputStream): ByteArray {
        val gis = GZIPInputStream(compressedData);
        val fos = ByteArrayOutputStream();

        val buffer = ByteArray(1024)
        var len: Int
        while ((gis.read(buffer).also { len = it }) != -1) {
            fos.write(buffer, 0, len)
        }

        return fos.toByteArray().also {
            compressedData.close()
            gis.close()
            fos.close()
        };
    }

    fun compressGZip(rawData : InputStream): ByteArray {
        val out = ByteArrayOutputStream();
        val gzipos = GZIPOutputStream(out);

        val buffer = ByteArray(1024)
        var len: Int
        while ((rawData.read(buffer).also { len = it }) != -1) {
            gzipos.write(buffer, 0, len)
        }

        //should be closed before return
        gzipos.close()

        return out.toByteArray().also {
            rawData.close()
            out.close();
        }
    }
}
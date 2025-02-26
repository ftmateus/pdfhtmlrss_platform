package pt.unl.fct.di.pdf_html_rss_core.utils

import java.io.InputStream
import java.security.MessageDigest
import java.util.*

private val base64Encoder =  Base64.getEncoder()

private fun toGenericHashByteArray(hashAlgorithm : String, stream : InputStream) : ByteArray {
    val hash = MessageDigest.getInstance(hashAlgorithm)
    val bufferSize = 4096;
    val buffer = ByteArray(bufferSize)

    stream.use {
        var bytesAvailable = it.available();
        while ( bytesAvailable > 0) {
            val bytesToWrite =
                if (bytesAvailable <= bufferSize)
                    bytesAvailable
                else
                    bufferSize;
            it.read(buffer)
            hash.update(buffer, 0, bytesToWrite)
            bytesAvailable = it.available();
        }
    }
    return hash.digest()
}

fun toSha1(stream : InputStream) : String {
    return toGenericHashByteArray("SHA-1", stream)
        .let { base64Encoder.encodeToString(it) }
}

fun toSha256(stream : InputStream) : String {
    return toGenericHashByteArray("SHA-256", stream)
        .let { base64Encoder.encodeToString(it) }
}

fun toSha256(data : ByteArray) : String {
    return toGenericHashByteArray("SHA-256", data.inputStream())
        .let { base64Encoder.encodeToString(it) }
}

fun toSha256ByteArray(data : ByteArray) : ByteArray {
    return toGenericHashByteArray("SHA-256", data.inputStream())
}

fun verifySha256(data : ByteArray, hash : String) : Boolean {
    return toSha256(data) == hash;
}

fun verifySha256(data : ByteArray, hash : ByteArray) : Boolean {
    return toSha256ByteArray(data).contentEquals(hash);
}

fun encodeAsHex(data : ByteArray) : String {
    return data
        .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
        .toString()
}
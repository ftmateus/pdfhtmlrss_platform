package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.*

@Service
class TemporaryFilesService {

    companion object {
        //1 hour
        const val TEMPORARY_FILES_TTL : Long = 3600 * 60 * 1000;
        val SPECIAL_TEMP_FILES_FOLDERS = listOf(
            SecurityService.SERIALIZED_KEYPAIR_FILE,
            "www.w3.com/"
        )
    }

    @Autowired
    lateinit var temporaryFolder : File;

    val temporaryFilesToDelete = mutableListOf<String>()

    fun writeToTempFile(
        name  : String? = null,
        deleteAutomatically : Boolean = true,
        customOutputStreamUse : (OutputStream) -> Unit
    ) : File {
        val fileName = name ?: UUID.randomUUID().toString()

        return File(temporaryFolder, fileName)
            .also { f ->
                f.createNewFile()
                f.outputStream().use {
                    customOutputStreamUse(it)
                }
                if(deleteAutomatically) {
                    f.deleteOnExit();
                    temporaryFilesToDelete.add(fileName)
                }
            }
    }

    /**
     * Śtream is automatically closed
     */
    fun writeToTempFile(
        stream : InputStream,
        name : String? = null,
        deleteAutomatically : Boolean = true
    ) : File {
        return writeToTempFile(name, deleteAutomatically) { out ->
            stream.use {
                stream.copyTo(out)
            }
        }
    }

    private fun isTemporaryFileExpired(file : File) : Boolean {
        if(!temporaryFilesToDelete.contains(file.name)) {
            return false
        }
        return file.lastModified() < System.currentTimeMillis() - TEMPORARY_FILES_TTL
    }

    fun getTempFileSecurely(fileName : String) : File? {
        if(SPECIAL_TEMP_FILES_FOLDERS.contains(fileName)) {
            return null;
        }

        return getTempFile(fileName)
    }

    fun getTempFile(fileName : String) : File? {
        val file = File(temporaryFolder, fileName)
        if(!file.exists())
            return null

        if(isTemporaryFileExpired(file)) {
            file.delete();
            temporaryFilesToDelete.remove(fileName)
            return null;
        }

        return file
    }

    //TODO every minute, maybe change it
    @Scheduled(fixedRate = 60*1000)
    private fun pruneTemporaryFiles() {
        for(temporaryFile in temporaryFilesToDelete) {
            val file = File(temporaryFolder, temporaryFile)
            if(isTemporaryFileExpired(file)) {
                temporaryFilesToDelete.remove(temporaryFile)
                file.delete()
            }
        }
    }
}
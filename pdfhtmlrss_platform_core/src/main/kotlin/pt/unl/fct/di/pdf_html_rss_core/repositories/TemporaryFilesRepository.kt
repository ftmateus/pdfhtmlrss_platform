package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.asSequence
import kotlin.streams.toList

@Repository
class TemporaryFilesRepository {

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

    fun makeTempSubFolder(folderName : String) : File {
        val subFolder = File(temporaryFolder, folderName)
        subFolder.mkdir();
        return subFolder
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
        if(fileName.isBlank())
            return null;

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

    fun createUnixNamedPipe() : File {
        val pipeFile = File(temporaryFolder, "fifo" + UUID.randomUUID().toString())

        ProcessBuilder("mkfifo", pipeFile.absolutePath).start().waitFor()

        pipeFile.deleteOnExit();
        return pipeFile
    }

    fun getNewTmpFileWithoutCreating(fileExtension : String = "") : File {
        return File(temporaryFolder, "${UUID.randomUUID()}${fileExtension}")
    }

    //Every 5 minutes
    @Scheduled(fixedRate = 5*60*1000)
    fun pruneTemporaryFiles() {
        val expiredFiles :List<File> = temporaryFilesToDelete.stream()
            .map { File(temporaryFolder, it) }
            .filter { isTemporaryFileExpired(it) }
            .toList()

        for(temporaryFile in expiredFiles) {
            temporaryFile.delete()
            temporaryFilesToDelete.remove(temporaryFile.name)
        }
    }
}
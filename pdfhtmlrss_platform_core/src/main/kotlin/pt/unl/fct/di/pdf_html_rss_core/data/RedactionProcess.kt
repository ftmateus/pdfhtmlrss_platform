package pt.unl.fct.di.pdf_html_rss_core.data

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

enum class RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}

/**
 * 30 minutes
 */
const val PENDING_REDACTION_TASK_TTL = 30 * 60L;

@RedisHash("redaction-processes", timeToLive = PENDING_REDACTION_TASK_TTL)
class RedactionProcess(
    @Id
    val taskId : String,
    val userId : Long,
    val fileType : String,
    val tmpPdfFile : String?,
    val tmpHtmlFile : String,
    val action : RedactionProcessAction
) {

    override fun equals(other: Any?): Boolean {
        if(other !is RedactionProcess)
            return false

        return (taskId == other.taskId)
            .and(userId == other.userId)
            .and(fileType == other.fileType)
            .and(tmpHtmlFile == other.tmpHtmlFile)
            .and(tmpPdfFile.equals(other.tmpPdfFile))
            .and(action == other.action)
    }

}
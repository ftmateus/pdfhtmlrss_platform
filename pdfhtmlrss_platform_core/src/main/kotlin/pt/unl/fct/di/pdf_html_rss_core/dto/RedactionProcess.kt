package pt.unl.fct.di.pdf_html_rss_core.dto

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.util.*
import javax.persistence.Entity

enum class RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}

/**
 * 30 minutes
 */
const val PENDING_REDACTION_TASK_TTL = 30 * 60L;

//@Entity
@RedisHash("redaction-processes", timeToLive = PENDING_REDACTION_TASK_TTL)
class RedactionProcess(
    @Id
    val taskId : String = UUID.randomUUID().toString(),
    val userId : Long,
    val fileType : String,
    val tmpPdfFile : String?,
    val tmpHtmlFile : String,
    val action : RedactionProcessAction
) {
    constructor() : this("", -1L, "", "", "", RedactionProcessAction.REDACT) {

    }

}
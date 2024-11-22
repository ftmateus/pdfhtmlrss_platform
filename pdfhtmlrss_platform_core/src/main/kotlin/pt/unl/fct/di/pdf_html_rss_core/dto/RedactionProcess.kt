package pt.unl.fct.di.pdf_html_rss_core.dto

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.util.*
import javax.persistence.Entity

enum class RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}

const val PENDING_REDACTION_TASK_TTL = 60 * 60 * 1000L;

//@Entity
@RedisHash("redaction-processes")
class RedactionProcess(
    @Id
    val taskId : String = UUID.randomUUID().toString(),
    val userId : String,
    val fileType : String,
    val tmpPdfFile : String?,
    val tmpHtmlFile : String,
    val expires : Long = System.currentTimeMillis() + PENDING_REDACTION_TASK_TTL,
    val action : RedactionProcessAction
) {
    constructor() : this("", "", "", "", "", 0L, RedactionProcessAction.REDACT) {

    }

}
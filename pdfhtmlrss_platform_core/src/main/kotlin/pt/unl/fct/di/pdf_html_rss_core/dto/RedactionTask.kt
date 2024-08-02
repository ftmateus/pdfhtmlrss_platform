package pt.unl.fct.di.pdf_html_rss_core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Generated
import org.springframework.http.MediaType
import java.util.*

enum class PendingRedactionTaskAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}

const val PENDING_REDACTION_TASK_TTL = 60 * 60 * 1000L;

data class PendingRedactionTask(
    val taskId : String = UUID.randomUUID().toString(),
    val userId : String,
    val fileType : String,
    val temporaryHtmlFile : String,
    val expires : Long = System.currentTimeMillis() + PENDING_REDACTION_TASK_TTL,
    val action : PendingRedactionTaskAction
)
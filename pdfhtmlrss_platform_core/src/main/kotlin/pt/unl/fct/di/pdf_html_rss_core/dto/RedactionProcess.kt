package pt.unl.fct.di.pdf_html_rss_core.dto

import java.util.*

enum class RedactionProcessAction {
    SELECT_REDACTABLE_ELEMS, REDACT
}

const val PENDING_REDACTION_TASK_TTL = 60 * 60 * 1000L;

data class RedactionProcess(
    val taskId : String = UUID.randomUUID().toString(),
    val userId : String,
    val fileType : String,
    val tmpPdfFile : String?,
    val tmpHtmlFile : String,
    val expires : Long = System.currentTimeMillis() + PENDING_REDACTION_TASK_TTL,
    val action : RedactionProcessAction
)
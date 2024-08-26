package pt.unl.fct.di.pdf_html_rss_core.dto

import java.security.KeyPair

data class User(
    val userId : Long?,
    val username : String?,
    val passwordHash : String?,
    val passwordClear : String?
)
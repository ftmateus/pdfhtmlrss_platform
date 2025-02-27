package pt.unl.fct.di.pdf_html_rss_core.data

data class AuthenticationStatus(
    val loggedIn: Boolean,
    val user : String?,
    val isAdmin: Boolean?,
)
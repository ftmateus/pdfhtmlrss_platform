package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class UserAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication {
        TODO("Not yet implemented")
    }

    override fun supports(authentication: Class<*>?): Boolean {
        TODO("Not yet implemented")
    }
}
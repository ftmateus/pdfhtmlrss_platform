package pt.unl.fct.di.pdf_html_rss_core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry
import org.springframework.security.web.SecurityFilterChain


@Configuration
class Config {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests(Customizer { authorizeRequests ->
            authorizeRequests.anyRequest()
                .permitAll()
        }) //TODO
        .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
        return http.build()
    }
}
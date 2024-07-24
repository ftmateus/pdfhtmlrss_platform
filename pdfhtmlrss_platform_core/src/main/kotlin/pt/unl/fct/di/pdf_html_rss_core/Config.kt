package pt.unl.fct.di.pdf_html_rss_core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry
import org.springframework.security.web.SecurityFilterChain
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


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

    @Bean
    fun documentBuilder() : DocumentBuilder  {
        val dbFactory = DocumentBuilderFactory
            .newInstance()

        //https://github.com/qzind/tray/commit/c04b510515246954a5a26475ae46434b7f127437
//        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        return dbFactory.newDocumentBuilder();
    }
}
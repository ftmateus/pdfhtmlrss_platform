package pt.unl.fct.di.pdf_html_rss_core

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.web.SecurityFilterChain
import java.io.File
import javax.servlet.annotation.WebServlet
import javax.sql.DataSource
import javax.xml.parsers.DocumentBuilderFactory


@Configuration
class Config {

    @Bean
    fun wpProvider() : WPProvider {
        return WPProvider()
    }

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
//    @Scope("prototype")
    fun documentBuilderFactory() : DocumentBuilderFactory  {
        val dbFactory = DocumentBuilderFactory
            .newInstance()

        dbFactory.isValidating = true;
//        dbFactory.isNamespaceAware = true

        //https://github.com/qzind/tray/commit/c04b510515246954a5a26475ae46434b7f127437
//        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

//        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);

//        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        return dbFactory;
//        return dbFactory.newDocumentBuilder();
    }

    @Bean
    fun temporaryFolder() : File {
        val systemTmpDirPath = System.getProperty("java.io.tmpdir");
        val tmpDirPath = "$systemTmpDirPath${File.separator}pdfrss";

        return File(tmpDirPath).also {
//            it.deleteRecursively()
            it.mkdir()
        }
    }

//    @Bean
//    fun h2servletRegistration(): ServletRegistrationBean<*> {
//        val registration: ServletRegistrationBean<*> = ServletRegistrationBean(WebServlet())
//        registration.addUrlMappings("/console/*")
//        return registration
//    }
}
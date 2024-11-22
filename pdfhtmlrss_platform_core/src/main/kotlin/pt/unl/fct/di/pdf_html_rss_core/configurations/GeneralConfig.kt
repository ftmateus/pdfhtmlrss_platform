package pt.unl.fct.di.pdf_html_rss_core.configurations

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


@Configuration
@EnableRedisRepositories
class GeneralConfig {

    @Bean
    fun wpProvider() : WPProvider {
        return WPProvider()
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

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<*, *> {
        val template = RedisTemplate<ByteArray, ByteArray>()
        template.setConnectionFactory(redisConnectionFactory!!)
        return template
    }

//    @Bean
//    fun h2servletRegistration(): ServletRegistrationBean<*> {
//        val registration: ServletRegistrationBean<*> = ServletRegistrationBean(WebServlet())
//        registration.addUrlMappings("/console/*")
//        return registration
//    }
}
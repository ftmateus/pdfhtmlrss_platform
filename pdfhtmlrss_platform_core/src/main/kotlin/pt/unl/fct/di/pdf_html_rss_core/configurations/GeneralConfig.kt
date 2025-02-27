package pt.unl.fct.di.pdf_html_rss_core.configurations

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory


@Configuration
@EnableRedisRepositories
@PropertySource("classpath:application-override.properties")
class GeneralConfig {

    @Bean
    fun wpProvider() : WPProvider = WPProvider()

    @Bean
    fun bcProvider() : BouncyCastleProvider = BouncyCastleProvider();

    @Bean
    fun passwordEncoder() : PasswordEncoder = BCryptPasswordEncoder(12)

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
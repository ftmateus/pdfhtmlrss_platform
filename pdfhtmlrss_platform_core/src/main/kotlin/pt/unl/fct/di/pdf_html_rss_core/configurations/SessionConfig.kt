package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession


//TODO serialize to JSON
@Configuration(proxyBeanMethods = false)
@EnableRedisHttpSession
class SessionConfig {
    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }
}
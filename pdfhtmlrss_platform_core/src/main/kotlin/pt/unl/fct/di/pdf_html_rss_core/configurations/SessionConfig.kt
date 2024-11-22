package pt.unl.fct.di.pdf_html_rss_core.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession


//TODO serialize to JSON https://docs.spring.io/spring-session/reference/getting-started/using-redis.html#serializing-session-using-json
@Configuration(proxyBeanMethods = false)
//@EnableRedisWebSession
@EnableRedisHttpSession(redisNamespace = "pdfhtmlrss")
class SessionConfig {

//    private var loader: ClassLoader? = null

    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }

//    @Bean
//    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
//        return GenericJackson2JsonRedisSerializer(objectMapper())
//    }
//
//    private fun objectMapper(): ObjectMapper {
//        val mapper = ObjectMapper()
//        mapper.registerModules(SecurityJackson2Modules.getModules(this.loader))
//        return mapper
//    }
//
//    override fun setBeanClassLoader(classLoader: ClassLoader) {
//        this.loader = classLoader;
//    }
}
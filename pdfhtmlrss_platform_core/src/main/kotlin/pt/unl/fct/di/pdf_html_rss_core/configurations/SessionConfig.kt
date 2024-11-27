package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession


//TODO serialize to JSON https://docs.spring.io/spring-session/reference/getting-started/using-redis.html#serializing-session-using-json
@Configuration(proxyBeanMethods = false)
//@EnableRedisWebSession
@EnableRedisHttpSession(redisNamespace = "pdfhtmlrss")
class SessionConfig {

//    private var loader: ClassLoader? = null

    @Value("\${spring.redis.host:localhost}")
    private val redisHost: String? = null

    @Value("\${spring.redis.port:6379}")
    private val redisPort = 0

    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(redisHost!!, redisPort)
        return LettuceConnectionFactory(redisConfig)
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
package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import pt.unl.fct.di.pdf_html_rss_core.services.UserService


@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Autowired
    private val userService: UserService? = null

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        return DaoAuthenticationProvider().also {
            it.setUserDetailsService(userService)
            //TODO move password encoder to bean
            it.setPasswordEncoder(BCryptPasswordEncoder(12))
        }
    }

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeRequests()
            .antMatchers("/test")
                .permitAll()
            .antMatchers("/login*")
                .permitAll()
            .anyRequest()
                .authenticated()
            .and()
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .csrf() //TODO
                .disable()
            .build()
//            .authenticationProvider(authenticationProvider)
    }
}
package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import pt.unl.fct.di.pdf_html_rss_core.services.UserService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


//https://www.geeksforgeeks.org/spring-security-authentication-providers/
@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Autowired
    private val userService: UserService? = null

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder;

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        return DaoAuthenticationProvider().also {
            it.setUserDetailsService(userService)
            it.setPasswordEncoder(passwordEncoder)
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
            .antMatchers("/auth/status")
                .permitAll()
            .anyRequest()
                .authenticated()
            .and()
//            .httpBasic(Customizer.withDefaults())
//            .formLogin(Customizer.withDefaults())
            .formLogin {
                it.successHandler { request, response, authentication ->
                    response.status = HttpServletResponse.SC_OK
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.writer.write("{\"loggedIn\":true,\"user\":\"${authentication.name}\"}")
                }
            }
            .logout {
                it.logoutSuccessHandler { request, response, authentication ->
                    response.status = HttpServletResponse.SC_OK
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.writer.write("{\"loggedIn\":false}")
                }
            }
            .csrf() //TODO
                .disable()
            .build()
//            .authenticationProvider(authenticationProvider)
    }
}
package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import pt.unl.fct.di.pdf_html_rss_core.services.UserService
import javax.servlet.http.HttpServletResponse


//https://www.geeksforgeeks.org/spring-security-authentication-providers/
@EnableWebSecurity
@Configuration
class SecurityConfig {

    val SWAGGER_AUTH_WHITELIST: Array<String> = arrayOf( // -- Swagger UI v2
        "/v2/api-docs",
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",  // -- Swagger UI v3 (OpenAPI)
        "/v3/api-docs/**",
        "/swagger-ui/**" // other public endpoints of your API may be appended to this array
    )

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
        return http
            .headers()
                .frameOptions()
                .sameOrigin()
            .and()
            .authorizeRequests()
            .antMatchers(*SWAGGER_AUTH_WHITELIST)
                .permitAll()
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
//                    response.contentType = MediaType.APPLICATION_JSON_VALUE
//                    val isAdmin = authentication.authorities
//                        .stream().anyMatch { a -> a.authority == "ROLE_ADMIN" }
//                    response.writer.write("{\"loggedIn\":true,\"user\":\"${authentication.name}\",\"isAdmin\":$isAdmin}")
                }
            }
            .logout {
                it.logoutSuccessHandler { request, response, authentication ->
                    response.status = HttpServletResponse.SC_OK
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                    response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"")
//                    response.contentType = MediaType.APPLICATION_JSON_VALUE
//                    response.writer.write("{\"loggedIn\":false}")
                }
            }
            .csrf() //TODO
                .disable()
            .build()
//            .authenticationProvider(authenticationProvider)
    }
}
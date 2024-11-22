package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import pt.unl.fct.di.pdf_html_rss_core.repositories.UsersRepository
import javax.annotation.PostConstruct
import kotlin.random.Random


@Service
class UserService : UserDetailsService {
    companion object {
        const val ADMIN_USER_ID = 0L;
    }

    @Autowired
    private lateinit var usersRepository: UsersRepository;

    @Autowired
    private lateinit var securityService: SecurityService;

    @PostConstruct
    final fun makeAdminUser()  {
        if(usersRepository.existsById(ADMIN_USER_ID)) {
            return;
        }

        val adminUser = User(
            ADMIN_USER_ID,
            "admin",
            "\$2a\$12\$An68OidsmncWv2WVCCiLLuWS3QN5mK7CaNvlLJQoo2m46ouFxCZZu",
            null
        )
        usersRepository.save(adminUser);
    }

    fun createUser(user : User) : Long {
        with(user.username) {
            check(isNotBlank())
            check(
                usersRepository.existsByUsername(this)
            )
        }
        with(user.passwordClear) {
            check(this != null)
            check(isNotBlank())
            check(length > 8)
            //TODO check if password is strong
        }

        val passwordHash = securityService
            .toSha256(user.passwordClear!!.toByteArray());

        val userId : Long = generateUserId();

        usersRepository.save(
            User(
                userId = userId,
                username = user.username,
                passwordHash = passwordHash,
                passwordClear = null
            )
        );
        securityService.generateKeyPairToUser(userId);

        return userId;
    }

    fun getUserById(userId: Long) : User {
        return usersRepository
            .findById(userId)
            //TODO change exception
            .orElseThrow { RuntimeException(); }
            .also {
                check(it.passwordClear == null);
            }
    }

    fun generateUserId() : Long {
        return generateSequence {
            Random.nextLong(100, Long.MAX_VALUE)
        }.first {
            !usersRepository.existsById(it)
        }
    }

    override fun loadUserByUsername(username: String): User {
        return usersRepository
            .findByUsername(username)
            //TODO change exception
            .orElseThrow { throw UsernameNotFoundException(username); }
            .also {
                check(it.passwordClear == null || it.passwordClear!!.isBlank());
            }
    }
}
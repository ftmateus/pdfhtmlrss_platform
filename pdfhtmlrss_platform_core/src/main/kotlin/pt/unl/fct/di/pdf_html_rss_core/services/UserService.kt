package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.repositories.UsersRepository
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

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder;

    @Value("\${pdfhtmlrss.user.admin.password}")
    private lateinit var adminPasswordHash: String

//    @PostConstruct
    final fun makeAdminUser()  {
        if(usersRepository.existsById(ADMIN_USER_ID)) {
            return;
        }

        val adminUser = User(
            ADMIN_USER_ID,
            "admin",
            adminPasswordHash,
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

        val passwordHash = passwordEncoder
            .encode(user.passwordClear)

        val userId : Long = generateUserId();

        usersRepository.save(
            User(
                userId = userId,
                username = user.username,
                passwordHash = passwordHash,
                passwordClear = null
            )
        );
        securityService.generateRSSKeyPairToUser(userId);

        return userId;
    }

    fun hasUserById(userId : Long) : Boolean {
        return usersRepository
            .existsById(userId)
    }

    fun hasUserByUsername(username : String) : Boolean {
        return usersRepository
            .existsByUsername(username)
    }

    fun loadUserById(userId: Long) : User {
        return usersRepository
            .findById(userId)
            //TODO change exception
            .orElseThrow { PDFHTMLRSSException(); }
            .also {
                check(it.passwordClear == null || it.passwordClear!!.isBlank());
            }
    }

    //TODO Maybe give responsability to database
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
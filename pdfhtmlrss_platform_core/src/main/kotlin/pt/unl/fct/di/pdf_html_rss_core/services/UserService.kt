package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import pt.unl.fct.di.pdf_html_rss_core.repositories.RSSKeyPairRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.UsersRepository
import javax.annotation.PostConstruct
import kotlin.random.Random

@Service
class UserService {
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
            username = "admin",
            userId = ADMIN_USER_ID,
            //TODO salt or use more sophisticated algorithm
            passwordHash = "fc8252c8dc55839967c58b9ad755a59b61b67c13227ddae4bd3f78a38bf394f7",
            passwordClear = null
        )
        usersRepository.save(adminUser);
        val keyPair = securityService
            .generateKeyPairToUser(ADMIN_USER_ID);
    }

    fun createUser(user : User) : Long {
        check(user.username != null);
        check(user.username.isNotBlank());
        check(user.passwordClear != null);
        check(user.passwordClear.isNotBlank());
        check(user.passwordClear.length > 8);
        //TODO check if password is strong

        check(
            usersRepository.existsByUsername(user.username)
        )

        val passwordHash = securityService
            .toSha256(user.passwordClear.toByteArray());

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
            .orElseThrow { RuntimeException(); }
            .also {
                check(it.passwordClear == null);
            }
    }

    fun getUserByUsername(username : String) : User {
        return usersRepository
            .findByUsername(username)
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

}
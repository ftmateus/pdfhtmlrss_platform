package pt.unl.fct.di.pdf_html_rss_core.components

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import pt.unl.fct.di.pdf_html_rss_core.services.PAdESService
import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import pt.unl.fct.di.pdf_html_rss_core.services.UserService


@Component
class StartupRunner {
    @Autowired
    private lateinit var userService: UserService;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository;

    @Autowired
    private lateinit var pAdESService: PAdESService;

    @Autowired
    private lateinit var securityService: SecurityService;

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        userService.makeAdminUser()

        val user = userService.loadUserByUsername("admin");

        securityService.setupKeyChainForUser(user);

        securityService.createRSSKeyPairForAdminUser();
    }
}
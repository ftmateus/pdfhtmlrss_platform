package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.PAdESKeyEntity
import pt.unl.fct.di.pdf_html_rss_core.data.User

@Repository
interface PAdESKeyRepository : JpaRepository<PAdESKeyEntity, Long> {
    fun findByUser(user: User): PAdESKeyEntity

    fun existsByUser(user: User) : Boolean;
}
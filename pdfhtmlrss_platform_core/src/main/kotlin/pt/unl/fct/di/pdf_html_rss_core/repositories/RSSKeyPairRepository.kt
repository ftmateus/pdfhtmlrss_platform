package pt.unl.fct.di.pdf_html_rss_core.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.RSSKeyPairEntity
import java.util.*
import pt.unl.fct.di.pdf_html_rss_core.data.User


@Repository
interface RSSKeyPairRepository : JpaRepository<RSSKeyPairEntity, Long> {
//    fun findRSSKeyPairEntityByUserIdAndAlgorithm(userId : Long, alg : String)
//    : Optional<RSSKeyPairEntity>;

    fun existsByUser(user: User): Boolean;
    
    fun existsByUserAndAlgorithm(user: User, algorithm: String): Boolean;

    fun findByUserAndAlgorithm(user: User, algorithm: String) : Optional<RSSKeyPairEntity>;
}
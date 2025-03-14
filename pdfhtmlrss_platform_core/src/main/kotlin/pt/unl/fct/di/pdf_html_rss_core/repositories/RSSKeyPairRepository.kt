package pt.unl.fct.di.pdf_html_rss_core.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.RSSKeyPairEntity
import java.util.*


@Repository
interface RSSKeyPairRepository : JpaRepository<RSSKeyPairEntity, Long> {
    fun findRSSKeyPairEntityByUserIdAndAlgorithm(userId : Long, alg : String)
    : Optional<RSSKeyPairEntity>;

    fun existsByUserId(userId: Long): Boolean;
    
    fun existsByUserIdAndAlgorithm(userId: Long, algorithm: String): Boolean;
}
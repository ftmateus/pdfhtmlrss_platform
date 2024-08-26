package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import java.security.KeyPair
import java.util.*

@Repository
class RSSKeyPairRepository {

    private final val keyPairs = mutableMapOf<Long, KeyPair>();

    fun saveKeyPair(userId : Long, rssKeyPair : KeyPair) {
        keyPairs[userId] = rssKeyPair;
    }

    fun getKeyPair(userId : Long) : KeyPair? {
        return keyPairs[userId]
    }
}
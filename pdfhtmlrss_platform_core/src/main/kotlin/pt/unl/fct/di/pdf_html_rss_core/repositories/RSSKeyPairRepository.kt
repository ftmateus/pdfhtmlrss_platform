package pt.unl.fct.di.pdf_html_rss_core.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.RSSKeyPairEntity


@Repository
interface RSSKeyPairRepository : JpaRepository<RSSKeyPairEntity, Long>
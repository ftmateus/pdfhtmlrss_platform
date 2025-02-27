package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.RedactionProcess

@Repository
interface RedactionProcessRepository : CrudRepository<RedactionProcess, String> {

}
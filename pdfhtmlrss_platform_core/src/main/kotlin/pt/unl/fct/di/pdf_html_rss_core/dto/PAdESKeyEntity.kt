package pt.unl.fct.di.pdf_html_rss_core.dto

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "padeskeys")
class PAdESKeyEntity {

    @Id
    var id: Long = 0L;
}
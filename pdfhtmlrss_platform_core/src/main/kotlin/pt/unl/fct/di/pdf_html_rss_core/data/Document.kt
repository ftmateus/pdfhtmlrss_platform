package pt.unl.fct.di.pdf_html_rss_core.data

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.lettuce.core.output.BooleanOutput
import javax.persistence.*

@JsonDeserialize
@Entity
@Table(name = "documents")
class Document(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user : User?,

    @Column(name = "path")
    var documentPath : String?,

    @Column(name = "redacted")
    var redacted : Boolean = false
) {
    constructor() : this(
        null,
        "") {
    }

    @Id
    @Column(name = "document_id")
    @GeneratedValue
    var documentId : Long? = null;
}
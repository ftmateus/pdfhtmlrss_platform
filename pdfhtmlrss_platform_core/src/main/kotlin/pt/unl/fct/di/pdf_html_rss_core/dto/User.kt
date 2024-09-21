package pt.unl.fct.di.pdf_html_rss_core.dto

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.security.KeyPair
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @Column(name = "userid")
    var userId : Long?,

    @Column(name = "username")
    var username : String?,

    @Column(name = "passwordhash")
    var passwordHash : String?,

    @Transient
    var passwordClear : String?,

) {
    constructor() : this(-1, "", "", "")

//    @OneToOne(optional = true, fetch = FetchType.LAZY)
//    var rssKeyPair: RSSKeyPairEntity? = null;
}
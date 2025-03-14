package pt.unl.fct.di.pdf_html_rss_core.data

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.*

@Entity
@Table(name = "rsskeypairs")
class RSSKeyPairEntity(

    @Id @Column(name = "rsskeyid")
    @GeneratedValue
    var rssKeyId : Long?,

    @Column(name = "userid")
    var userId : Long?,

    @Column(name = "algorithm")
    var algorithm : String?,

    @Lob
    @Column(name = "privatekey")
    var privateKey : PrivateKey?,

    @Lob
    @Column(name = "publickey")
    var publicKey : PublicKey?,
) {
    constructor() : this(-1, -1,null, null, null);

    @get:Transient
    val keyPair : KeyPair get() {
        return KeyPair(publicKey, privateKey)
    }

    //@OneToOne(optional = false, mappedBy = "userId")
    //lateinit var user : User;
}
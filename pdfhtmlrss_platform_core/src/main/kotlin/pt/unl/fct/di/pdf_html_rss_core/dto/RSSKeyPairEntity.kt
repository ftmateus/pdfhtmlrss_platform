package pt.unl.fct.di.pdf_html_rss_core.dto

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.security.Key
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.*

@Entity
@Table(name = "rsskeypairs")
class RSSKeyPairEntity(
    @Id @Column(name = "userid")
    var userId : Long?,

    @Lob
    @Column(name = "privatekey")
    var privateKey : PrivateKey?,

    @Lob
    @Column(name = "publickey")
    var publicKey : PublicKey?,
) {
    constructor() : this(-1, null, null);

    @get:Transient
    val keyPair : KeyPair get() {
        return KeyPair(publicKey, privateKey)
    }

    //@OneToOne(optional = false, mappedBy = "userId")
    //lateinit var user : User;
}
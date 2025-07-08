package pt.unl.fct.di.pdf_html_rss_core.data

import pt.unl.fct.di.pdf_html_rss_core.components.PAdESPrivateKeyDatabaseEncrypt
import java.security.interfaces.RSAPrivateKey
import java.security.cert.Certificate
import javax.persistence.*

@Entity
@Table(name = "padeskeys")
class PAdESKeyEntity(
    @OneToOne
    @JoinColumn(name = "user_id")
    var user: User?,

    @Lob
    @Column(name = "privatekey")
    @Convert(converter = PAdESPrivateKeyDatabaseEncrypt::class)
    var privateKey: RSAPrivateKey?,

    @Lob
    @Column(name = "certificate")
    var certificate: Certificate?
) {
    constructor() : this(null, null, null);

    @Id
    @Column(name = "key_pair_id")
    @GeneratedValue
    var id : Long? = null;
}
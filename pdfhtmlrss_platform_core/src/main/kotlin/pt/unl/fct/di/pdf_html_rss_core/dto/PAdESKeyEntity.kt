package pt.unl.fct.di.pdf_html_rss_core.dto

import pt.unl.fct.di.pdf_html_rss_core.components.DatabaseKeysEncrypt
import java.security.interfaces.RSAPrivateKey
import java.security.cert.Certificate
import javax.persistence.*

@Entity
@Table(name = "padeskeys")
class PAdESKeyEntity(
    @Id
    var userId: Long = 0L,

    @Lob
    @Column(name = "privatekey")
    @Convert(converter = DatabaseKeysEncrypt::class)
    var privateKey: RSAPrivateKey?,

    @Lob
    @Column(name = "certificate")
    var certificate: Certificate?
) {
    constructor() : this(-1, null, null);

}
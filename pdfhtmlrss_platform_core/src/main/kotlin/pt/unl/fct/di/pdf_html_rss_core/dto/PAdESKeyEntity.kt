package pt.unl.fct.di.pdf_html_rss_core.dto

import java.security.PrivateKey
import java.security.cert.Certificate
import javax.persistence.*

@Entity
@Table(name = "padeskeys")
class PAdESKeyEntity(
    @Id
    var userId: Long = 0L,

    @Lob
    @Column(name = "privatekey")
    var privateKey: PrivateKey?,

    @Lob
    @Column(name = "certificate")
    var certificate: Certificate?
) {
    constructor() : this(-1, null, null);

}
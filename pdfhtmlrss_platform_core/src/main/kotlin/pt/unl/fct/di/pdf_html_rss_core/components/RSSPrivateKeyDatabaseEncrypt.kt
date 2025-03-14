package pt.unl.fct.di.pdf_html_rss_core.components

import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import javax.persistence.AttributeConverter
import javax.persistence.Converter


//@Converter(autoApply = true)
class RSSPrivateKeyDatabaseEncrypt : AttributeConverter<PrivateKey, ByteArray> {

    override fun convertToDatabaseColumn(privateKey: PrivateKey): ByteArray {
        val securityService: SecurityService = ApplicationContextProvider?.getBean(SecurityService::class.java)
            ?: throw AssertionError()

//        val pkcs8Data = securityService.toPKCS8RSAPrivateKey(privateKey);

//        return securityService.encryptData(pkcs8Data);

        TODO()
    }

    override fun convertToEntityAttribute(encryptedPkcs8Data: ByteArray): RSAPrivateKey {
        val securityService: SecurityService = ApplicationContextProvider?.getBean(SecurityService::class.java)
            ?: throw AssertionError()

        TODO()

//        val decryptedPkcs8Data = securityService.decryptData(encryptedPkcs8Data);
//
//        return securityService.fromPKCS8RSAPrivateKey(decryptedPkcs8Data);
    }
}
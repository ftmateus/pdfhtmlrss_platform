package pt.unl.fct.di.pdf_html_rss_core.components

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import pt.unl.fct.di.pdf_html_rss_core.services.SecurityService
import java.security.interfaces.RSAPrivateKey
import javax.persistence.AttributeConverter
import javax.persistence.Converter


@Converter(autoApply = true)
class DatabaseKeysEncrypt : AttributeConverter<RSAPrivateKey, ByteArray> {


    override fun convertToDatabaseColumn(privateKey: RSAPrivateKey): ByteArray {
        val securityService: SecurityService = ApplicationContextProvider?.getBean(SecurityService::class.java)
            ?: throw AssertionError()

        val pkcs8Data = securityService.toPKCS8RSAPrivateKey(privateKey);

        return securityService.encryptData(pkcs8Data);
    }

    override fun convertToEntityAttribute(encryptedPkcs8Data: ByteArray): RSAPrivateKey {
        val securityService: SecurityService = ApplicationContextProvider?.getBean(SecurityService::class.java)
            ?: throw AssertionError()

        val decryptedPkcs8Data = securityService.decryptData(encryptedPkcs8Data);

        return securityService.fromPKCS8RSAPrivateKey(decryptedPkcs8Data);
    }
}
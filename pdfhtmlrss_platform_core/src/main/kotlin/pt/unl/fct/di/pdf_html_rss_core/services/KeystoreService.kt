package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.annotation.PostConstruct
import javax.crypto.SecretKey

@Service
class KeystoreService() {
    @Value("\${pdfhtmlrss.keystore.path}")
    private lateinit var keystorePath: String;

    @Value("\${pdfhtmlrss.keystore.password}")
    private lateinit var keystorePassword: String;

    private lateinit var keystore: KeyStore;

    private var redactionCACertificate: Certificate? = null;
    private var redactionCAPrivateKey: PrivateKey? = null;
    private var secretKey: SecretKey? = null;

    @PostConstruct
    fun postConstruct() {
        val keyStoreFile = File(keystorePath)
        if(!keyStoreFile.exists())
            throw FileNotFoundException("Keystore not found")

        keystore = KeyStore.getInstance(when(keyStoreFile.extension) {
            "jks" -> "JKS";
            "pem" -> "PKCS12"
            else -> TODO()
        })
        keyStoreFile.inputStream().use {
            keystore.load(it, keystorePassword.toCharArray())
        }
    }

    fun getRedactionCAPrivateKey() : PrivateKey {
        if(redactionCAPrivateKey == null)
            redactionCAPrivateKey = keystore.getKey("pdfhtmlrss_ca", keystorePassword.toCharArray())
                    as PrivateKey

        return redactionCAPrivateKey as PrivateKey;
    }

    fun getRedactionCACertificate(): X509Certificate? {
        if(redactionCAPrivateKey == null)
            redactionCACertificate = keystore.getCertificate("pdfhtmlrss_ca")
                    as X509Certificate

        return redactionCACertificate as X509Certificate;
    }

    fun getEncryptionKey() : SecretKey {
        if(secretKey == null)
            secretKey = keystore.getKey("pdfhtmlrss_encryption_key", keystorePassword.toCharArray())
                    as SecretKey

        return secretKey as SecretKey;
    }

}
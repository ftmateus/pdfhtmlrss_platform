package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.springframework.stereotype.Service
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


@Service
class SecurityService() {

    init {
        Security.insertProviderAt(WPProvider(), 1)
    }

    val keyPair : KeyPair = let {
        val serializedFile = File("keyPair.ser");
        if(serializedFile.exists()) {
            readSerializedKeyPair(serializedFile)
        } else {
            val keyPair = generateKeyPair();
            serializeKeyPair(keyPair, serializedFile);
            keyPair;
        }
    };

    val publicKey : PublicKey get() = keyPair.public;
    val privateKey : PrivateKey get() = keyPair.private;

    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("GSRSSwithRSAandBPA")
        //TODO change key size
        keyGen.initialize(1024)
        return keyGen.generateKeyPair()
    }

    @Throws(IOException::class)
    private fun serializeKeyPair(keyPair: KeyPair, file : File) {
        ObjectOutputStream(FileOutputStream(file))
        .use { out ->
            out.writeObject(keyPair.public)
            out.writeObject(keyPair.private)
        }
    }

    fun readSerializedKeyPair(serializedFile : File) : KeyPair {
        return ObjectInputStream(FileInputStream(serializedFile))
        .use { objIn ->
            val publicKey = objIn.readObject() as PublicKey;
            val privateKey = objIn.readObject() as PrivateKey;
            KeyPair(publicKey, privateKey)
        }
    }

    fun readX509PublicKey(filePath: String): PublicKey {
        val file = File(filePath);
        val key = String(Files.readAllBytes(file.toPath()), Charset.defaultCharset())
        val publicKeyPEM = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END PUBLIC KEY-----", "")

        val encoded: ByteArray = Base64
            .getDecoder()
            .decode(publicKeyPEM)

        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as PublicKey
    }

    fun readPKCS8PrivateKey(filePath: String): PrivateKey {
        val file = File(filePath);
        val key = String(Files.readAllBytes(file.toPath()), Charset.defaultCharset())
        val privateKeyPEM = key
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END PRIVATE KEY-----", "")

        val encoded: ByteArray = Base64
            .getDecoder()
            .decode(privateKeyPEM)

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as PrivateKey
    }

    fun toSha256(data : ByteArray) : String {
        //TODO improve performance?
        return MessageDigest.getInstance("SHA-256")
            .digest(data)
            .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
            .toString()
    }

    fun verifySha256(data : ByteArray, hash : String) : Boolean {
        return toSha256(data) == hash;
    }
}
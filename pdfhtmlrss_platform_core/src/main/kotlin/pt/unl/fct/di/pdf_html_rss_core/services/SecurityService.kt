package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.RSSKeyPairEntity
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.repositories.RSSKeyPairRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.annotation.PostConstruct


@Service
class SecurityService() {

    var logger: Logger = LoggerFactory.getLogger(SecurityService::class.java)

    @Autowired
    private lateinit var rssKeyPairRepository: RSSKeyPairRepository;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    private lateinit var wpProvider: WPProvider

//    lateinit var keyPair : KeyPair;

//    val publicKey : PublicKey get() = keyPair.public;
//    val privateKey : PrivateKey get() = keyPair.private;

    companion object {
        const val SERIALIZED_KEYPAIR_FILE = "keyPair.ser"
    }

    @PostConstruct
    private fun afterBeanInitialization() {
        Security.insertProviderAt(wpProvider, 1)

        rssKeyPairRepository
            .findById(UserService.ADMIN_USER_ID)
            .orElseGet {
                logger.info("Admin key pair not found on database, generating new one...")
                generateKeyPairToUser(UserService.ADMIN_USER_ID)
            }
            .keyPair
//        keyPair = getSerializedKeyPair()
    }

    private fun getSerializedKeyPair() : KeyPair {
        val serializedFile : File? = temporaryFilesRepository.getTempFile(SERIALIZED_KEYPAIR_FILE);
        if(serializedFile != null) {
            return readSerializedKeyPair(serializedFile)
        }
        logger.info("Key pair not found, generating new one...")

        return generateKeyPair().also { kp ->
            temporaryFilesRepository.writeToTempFile(
                SERIALIZED_KEYPAIR_FILE,
                deleteAutomatically = false
            ) { out ->
                serializeKeyPair(kp, out)
            }
        }
    }

    fun getLoggedInUser() : User? {
        val securityContext: SecurityContext = SecurityContextHolder.getContext()
        val authentication: Authentication = securityContext.authentication
        val principal: Any = authentication.principal
        return if (principal is User) principal else null
    }

    fun getKeyPairFromLoggedInUser() : RSSKeyPairEntity {
        val currentUser = getLoggedInUser() ?: throw PDFHTMLRSSException();

        return rssKeyPairRepository
            .findById(currentUser.userId)
            .orElseThrow { PDFHTMLRSSException() }
    }

    fun generateKeyPairToUser(userId : Long) : RSSKeyPairEntity {
        //check(rssKeyPairRepository.findById(userId) == null)

        val keyPair = generateKeyPair()

        return rssKeyPairRepository.save(
            RSSKeyPairEntity(
                userId, keyPair.private, keyPair.public
            )
        )
    }

    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("GSRSSwithRSAandBPA")
        //TODO Key generation is too slow...
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    @Throws(IOException::class)
    private fun serializeKeyPair(keyPair: KeyPair, outputStream: OutputStream) {
        outputStream.use {
            ObjectOutputStream(it).use { out ->
                out.writeObject(keyPair.public)
                out.writeObject(keyPair.private)
            }
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

    private fun toGenericHash(hashAlgorithm : String, stream : InputStream) : String {
        val hash = MessageDigest.getInstance(hashAlgorithm)
        val bufferSize = 4096;
        val buffer : ByteArray = ByteArray(bufferSize)
        stream.use {
            var bytesAvailable = it.available();
            while ( bytesAvailable > 0) {
                val bytesToWrite = let {
                    if (bytesAvailable <= bufferSize)
                        bytesAvailable
                    else
                        bufferSize;
                }
                it.read(buffer)
                hash.update(buffer, 0, bytesToWrite)
                bytesAvailable = it.available();
            }
        }
        return hash.digest()
            .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
            .toString()
    }

    fun toSha1(stream : InputStream) : String {
        return toGenericHash("SHA-1", stream);
    }

    fun toSha256(stream : InputStream) : String {
        return toGenericHash("SHA-256", stream);
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
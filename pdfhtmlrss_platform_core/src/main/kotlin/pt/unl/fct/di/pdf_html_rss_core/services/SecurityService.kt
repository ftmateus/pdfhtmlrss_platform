package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemObjectGenerator
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PAdESKeyEntity
import pt.unl.fct.di.pdf_html_rss_core.dto.RSSKeyPairEntity
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.repositories.PAdESKeyRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.RSSKeyPairRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.*
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.*
import java.security.cert.Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.annotation.PostConstruct
import javax.crypto.Cipher


@Service
class SecurityService() {

    var logger: Logger = LoggerFactory.getLogger(SecurityService::class.java)

    @Autowired
    private lateinit var rssKeyPairRepository: RSSKeyPairRepository;

    @Autowired
    private lateinit var pAdESKeyRepository: PAdESKeyRepository;

    @Autowired
    private lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    private lateinit var wpProvider: WPProvider

    @Autowired
    private lateinit var bcProvider: BouncyCastleProvider

    @Autowired
    private lateinit var keystoreService: KeystoreService;


//    lateinit var keyPair : KeyPair;

//    val publicKey : PublicKey get() = keyPair.public;
//    val privateKey : PrivateKey get() = keyPair.private;

    companion object {
        const val SERIALIZED_KEYPAIR_FILE = "keyPair.ser"
        const val RSS_KEY_SIZE = 2048
        const val PADES_RSA_KEY_SIZE = 2048
    }

    @PostConstruct
    private fun addSecurityProviders() {
        Security.insertProviderAt(wpProvider, 1)
        Security.insertProviderAt(bcProvider, 2)
    }

    fun createRSSKeyPairForAdminUser() {
        rssKeyPairRepository
            .findById(UserService.ADMIN_USER_ID)
            .orElseGet {
                logger.info("Admin key pair not found on database, generating new one...")
                generateRSSKeyPairToUser(UserService.ADMIN_USER_ID)
            }
    }

    fun getLoggedInUser() : User? {
        val securityContext: SecurityContext = SecurityContextHolder.getContext()
        val authentication: Authentication = securityContext.authentication
        val principal: Any = authentication.principal
        return if (principal is pt.unl.fct.di.pdf_html_rss_core.dto.User)
            principal
        else null
    }

    fun getRSSKeyPairFromLoggedInUser() : RSSKeyPairEntity {
        val currentUser = getLoggedInUser() ?: throw PDFHTMLRSSException();

        return rssKeyPairRepository
            .findById(currentUser.userId)
            .orElseThrow { PDFHTMLRSSException() }
    }

    fun generateRSSKeyPairToUser(userId : Long) : RSSKeyPairEntity {
        //check(rssKeyPairRepository.findById(userId) == null)

        val keyPair = generateRSSKeyPair()

        return rssKeyPairRepository.save(
            RSSKeyPairEntity(
                userId, keyPair.private, keyPair.public
            )
        )
    }

    fun getLoggedInUserPadesKey() : PAdESKeyEntity {
        val user = getLoggedInUser();
        return pAdESKeyRepository.findById(user!!.userId)
            .orElseThrow()
    }

    fun encryptData(data : ByteArray) : ByteArray {
        val encryptionKey = keystoreService.getEncryptionKey();
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)

        return cipher.doFinal(data);
    }

    fun decryptData(encryptedData : ByteArray) : ByteArray {
        val encryptionKey = keystoreService.getEncryptionKey();
        val cipher: Cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey)

        return cipher.doFinal(encryptedData);
    }

    fun generateRSAKeyPair() : KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(PADES_RSA_KEY_SIZE)
        return keyGen.generateKeyPair()
    }

    fun setupKeyChainForUser(user : User) {
        if(pAdESKeyRepository.existsById(user.userId).not()) {
            val padesRsaKeyPair = generateRSAKeyPair();

            val userCertificate = generateUserCertificate(user, padesRsaKeyPair.public);

            pAdESKeyRepository.save(
                PAdESKeyEntity(user.userId, padesRsaKeyPair.private as RSAPrivateKey, userCertificate)
            )
        }

        if(rssKeyPairRepository.existsById(user.userId).not()) {
            generateRSSKeyPairToUser(user.userId)
        }
    }

    fun generateUserCertificate(user : User, userPublicKey: PublicKey) : Certificate {
        //TODO
        val subjectDN = "CN=PDFHTMLRSS, O=PDFHTMLRSS, L=PDFHTMLRSS, C=PDFHTMLRSS"

        val redactionCACertificate = keystoreService.getRedactionCACertificate();
        val redactionCAPrivateKey = keystoreService.getRedactionCAPrivateKey();

        val issuer = X500Name("CN=PDFHTMLRSS, O=PDFHTMLRSS, C=US") // CA DN
        val subject = X500Name("CN=${user.username}, O=PDFHTMLRSS, C=US") // Client DN

        val notBefore = Date()
        val notAfter = Date(System.currentTimeMillis() + 2 * 365L * 24 * 60 * 60 * 1000) // 2 years validity

        // Serial number for the certificate
        val serialNumber = BigInteger.valueOf(System.currentTimeMillis())

        // Create the certificate builder
        val certificateBuilder = X509v3CertificateBuilder(
            issuer,
            serialNumber,
            notBefore,
            notAfter,
            subject,
            SubjectPublicKeyInfo.getInstance(userPublicKey.encoded),
        )


        // Sign the certificate using the CA private key
        val contentSigner: ContentSigner = JcaContentSignerBuilder("SHA256withRSA")
            .setProvider(bcProvider.name)
            .build(redactionCAPrivateKey)


        // Generate the certificate
        return JcaX509CertificateConverter()
            .setProvider(bcProvider.name)
            .getCertificate(certificateBuilder.build(contentSigner))

    }

    fun generateRSSKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("GSRSSwithRSAandBPA")
        //TODO Key generation is too slow...
        keyGen.initialize(RSS_KEY_SIZE)
        return keyGen.generateKeyPair()
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

    fun fromPKCS8RSAPrivateKey(privateKeyData : ByteArray): RSAPrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA")

        privateKeyData.inputStream().use {
            PemReader(it.bufferedReader()).use { pemReader ->
                val pemObject: PemObject = pemReader.readPemObject()
                val content = pemObject.content
                val privateKeySpec = PKCS8EncodedKeySpec(content)
                return keyFactory.generatePrivate(privateKeySpec) as RSAPrivateKey
            }
        }
    }

    fun toPKCS8RSAPrivateKey(privateKey : RSAPrivateKey) : ByteArray {
        ByteArrayOutputStream().use {
            PemWriter(it.bufferedWriter()).use { pemWriter ->
                pemWriter.writeObject(PemObject("RSA Private Key", privateKey.encoded))
            }
            return it.toByteArray()
        }
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
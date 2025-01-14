package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.StampingProperties
import com.itextpdf.signatures.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.OutputStream
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import com.itextpdf.signatures.SignatureUtil;
import java.io.ByteArrayOutputStream


@Service
class PAdESService {

    @Autowired
    private lateinit var securityService: SecurityService;

    @Autowired
    private lateinit var keystoreService: KeystoreService;

    @Autowired
    private lateinit var bcProvider: BouncyCastleProvider

//    @Value("\${pdfhtmlrss.keystore.path}")
//    private lateinit var keystorePath: String;
//
//    @Value("\${pdfhtmlrss.keystore.password}")
//    private lateinit var keystorePassword: String;
//
    private lateinit var keystore: KeyStore;

    companion object {
        const val SIGNATURE_FIELD = "PDFHTMLRSS"
    }

//    fun signDocument(pdfFile : PDFFileWrapper) : ByteArray {
//        ByteArrayOutputStream().use { baos ->
//            signDocument(pdfFile, baos);
//        }
//    }

    //https://github.com/itext/itext-publications-signatures-java/blob/develop/src/test/java/com/itextpdf/samples/signatures/chapter02/C2_01_SignHelloWorld.java
    fun signDocument(pdfFile : PDFFileWrapper, outputStream: OutputStream) {

        val userKey = securityService.getLoggedInUserPadesKey();

        val redactionCACertificate = keystoreService.getRedactionCACertificate()
            as Certificate;

        //TODO close input stream
        PdfReader(pdfFile.getInputStream()).use { reader ->
            val signer = PdfSigner(reader, outputStream, StampingProperties())
            signer.fieldName = SIGNATURE_FIELD;


            val pks = PrivateKeySignature(userKey.privateKey, DigestAlgorithms.SHA256, bcProvider.name)
            val digest: IExternalDigest = BouncyCastleDigest()

            signer.signDetached(digest, pks, arrayOf(userKey.certificate, redactionCACertificate), null, null, null, 0, PdfSigner.CryptoStandard.CADES);
        }
    }

    fun verifyDocument(pdfFile : PDFFileWrapper) : Boolean {
        return pdfFile.useItextKernelPdfDocument {
            val sigUtil = SignatureUtil(it);
            val pkcs7 = sigUtil.readSignatureData(SIGNATURE_FIELD)
            sigUtil.signatureCoversWholeDocument(SIGNATURE_FIELD)
                    && pkcs7.verifySignatureIntegrityAndAuthenticity();
        }
    }
}
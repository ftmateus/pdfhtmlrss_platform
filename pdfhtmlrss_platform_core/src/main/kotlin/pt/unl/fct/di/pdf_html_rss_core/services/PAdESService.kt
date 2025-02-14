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
import java.security.cert.Certificate
import com.itextpdf.signatures.SignatureUtil;


@Service
class PAdESService {

    @Autowired
    private lateinit var securityService: SecurityService;

    @Autowired
    private lateinit var keystoreService: KeystoreService;

    @Autowired
    private lateinit var bcProvider: BouncyCastleProvider

    companion object {
        const val SIGNATURE_FIELD = "PDFHTMLRSS"
    }

    //https://github.com/itext/itext-publications-signatures-java/blob/develop/src/test/java/com/itextpdf/samples/signatures/chapter02/C2_01_SignHelloWorld.java
    fun signDocument(pdfFile : PDFFileWrapper, outputStream: OutputStream) {

        val userKey = securityService.getLoggedInUserPadesKey();

        val redactionCACertificate = keystoreService.getRedactionCACertificate()
            as Certificate;

        //TODO close input stream
        pdfFile.getInputStream().use {
            PdfReader(it).use { reader ->
                val signer = PdfSigner(reader, outputStream, StampingProperties())
                signer.fieldName = SIGNATURE_FIELD;

                val pks = PrivateKeySignature(userKey.privateKey, DigestAlgorithms.SHA256, bcProvider.name)
                val digest: IExternalDigest = BouncyCastleDigest()

                signer.signDetached(digest, pks, arrayOf(userKey.certificate, redactionCACertificate), null, null, null, 0, PdfSigner.CryptoStandard.CADES);
            }
        }
    }

    fun verifyDocument(sigUtil : SignatureUtil) : Boolean {
        val pkcs7 = sigUtil.readSignatureData(SIGNATURE_FIELD)
        return sigUtil.signatureCoversWholeDocument(SIGNATURE_FIELD)
                && pkcs7.verifySignatureIntegrityAndAuthenticity();
    }

    fun verifyDocument(pdfFile : PDFFileWrapper) : Boolean {
        return pdfFile.useItextKernelPdfDocument {
            val sigUtil = SignatureUtil(it);
            verifyDocument(sigUtil)
        }
    }
}
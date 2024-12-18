package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.StampingProperties
import com.itextpdf.signatures.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import java.io.OutputStream
import java.security.KeyStore
import java.security.cert.Certificate


@Service
class PAdESService {

    @Autowired
    private lateinit var securityService: SecurityService;

    fun signDocument(pdfFile : PDFFileWrapper, outputStream: OutputStream) {
        val privateKey = securityService.getKeyPairFromLoggedInUser().privateKey;

        //TODO close input stream
        PdfReader(pdfFile.getInputStream()).use { reader ->
            val signer = PdfSigner(reader, outputStream, StampingProperties())
            signer.fieldName = "Signature1";

            val pks = PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "BC")
            val digest: IExternalDigest = BouncyCastleDigest()

            signer.signDetached(digest, pks, arrayOf(), null, null, null, 0, PdfSigner.CryptoStandard.CADES);
        }
    }

    fun verifyDocument(pdfFile : PDFFileWrapper) : PDFFileWrapper {
        TODO()
    }

    fun loadRootCertificate() : Certificate {
        val keyStore = KeyStore.getInstance("PKCS12")

        TODO()
    }
}
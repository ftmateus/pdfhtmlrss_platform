package pt.unl.fct.di.pdf_html_rss_core.services

import com.itextpdf.kernel.pdf.*
import com.itextpdf.signatures.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.utils.toSha256
import java.io.OutputStream
import java.security.cert.Certificate


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

        val pdfFileInputStream = pdfFile.getInputStream();
        val reader = PdfReader(pdfFileInputStream)
        try {
            val signer = PdfSigner(reader, outputStream, StampingProperties())
            signer.fieldName = SIGNATURE_FIELD;
            signer.certificationLevel = PdfSigner.CERTIFIED_NO_CHANGES_ALLOWED;
            
            val pks = PrivateKeySignature(userKey.privateKey, DigestAlgorithms.SHA256, bcProvider.name)
            val digest: IExternalDigest = BouncyCastleDigest()

//            signXHTMLRSSSignatureIfPresent(signer)

            signer.signDetached(digest, pks, arrayOf(userKey.certificate, redactionCACertificate),
                null, null, null, 0, PdfSigner.CryptoStandard.CADES);

        } finally {
            reader.close()
            pdfFileInputStream.close()
        }
    }

    private fun signXHTMLRSSSignatureIfPresent(signer : PdfSigner) {
        val catalog: PdfDictionary = signer.document.catalog.pdfObject
        val names: PdfDictionary? = catalog.getAsDictionary(PdfName.Names)
        val embeddedFiles : PdfArray? = names?.getAsDictionary(PdfName.EmbeddedFiles)
            ?.getAsArray(PdfName.Names)

        if(embeddedFiles == null || embeddedFiles.isEmpty)
            return;

        var xhtmlRssSigIdx : Int = -1;

        for(i in 0..embeddedFiles.size()) {
            val filename = embeddedFiles.getAsString(i)
            if(filename.toString() == PDFManipulationService.ATTACHED_FULL_RSS_FILE_NAME) {
                xhtmlRssSigIdx = i;
                break;
            }
        }

        if(xhtmlRssSigIdx == -1)
            return;

        val fileSpec = embeddedFiles.getAsDictionary(xhtmlRssSigIdx + 1)

        val fileStream: PdfStream = fileSpec.getAsDictionary(PdfName.EF)
            .getAsStream(PdfName.F)

        val hash = toSha256(fileStream.bytes)

        signer.signatureDictionary
            .put(PdfName("XMLRSSHash"), PdfString(hash))
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
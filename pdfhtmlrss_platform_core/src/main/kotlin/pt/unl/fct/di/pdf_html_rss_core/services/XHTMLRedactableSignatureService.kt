package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.utils.XMLUtils
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Node

@Service
class XHTMLRedactableSignatureService {

    @Autowired
    lateinit var securityService : SecurityService;

    @Autowired
    lateinit var domService : DOMService;

    companion object {
        const val DEFAULT_RSS_ALGORITHM = "GSRSSwithRSAandBPA";
        val SUPPORTED_RSS_ALGORITHMS = arrayOf(
            "GSRSSwithRSAandBPA",
            "GLRSSwithRSAandBPA",
            "PSRSS"
        )
    }

    fun signAndRedactDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val signedDoc = signDocument(doc, redactSelectors);

        if(redactSelectors.isEmpty())
            return signedDoc

        return redactDocument(signedDoc, redactSelectors);
    }

    fun signDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val keyPair = securityService
            .getRSSKeyPairFromLoggedInUser()
            .keyPair;

        val rss = RedactableXMLSignature.getInstance(DEFAULT_RSS_ALGORITHM);

        rss.initSign(keyPair);
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addSignSelector(selector, true)

        return rss.sign(true);
    }

    fun signDocumentWithSeparatedSignature(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(DEFAULT_RSS_ALGORITHM);

        val keyPair = securityService
            .getRSSKeyPairFromLoggedInUser()
            .keyPair;

        rss.initSign(keyPair);
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addSignSelector(selector, true)

        return rss.signSeparate(true);
    }

    fun redactDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(DEFAULT_RSS_ALGORITHM);

        //public key is retrieved from signed file
        rss.initRedact(null)
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addRedactSelector(selector)

        return rss.redact();
    }

    fun verifyDocument(signedDoc : Document) : Boolean {
        val sig = RedactableXMLSignature.getInstance(DEFAULT_RSS_ALGORITHM);

        //public key is retrieved from signed file
        sig.initVerify(null);

        sig.setDocument(signedDoc)

        return sig.verify()
    }

    //TODO use XMLRSS Signature class
    fun verifyDocument(signedDoc : Document, signatureData : Document) : Boolean {
        val signatureElem = signatureData.documentElement;

        signedDoc.documentElement.appendChild(
            signedDoc.importNode(signatureElem, true)
        )

        domService.printDocument(signedDoc)

        return verifyDocument(signedDoc)
    }

    fun extractRSSSignature(domDoc : Document) : Node {
//        return Dereferencer.dereference("SignatureInfo", domDoc)
        return XMLUtils.getSignatureNode(domDoc.documentElement)
    }
}
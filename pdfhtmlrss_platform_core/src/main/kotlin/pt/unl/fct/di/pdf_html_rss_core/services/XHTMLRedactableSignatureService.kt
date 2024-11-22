package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.grss.xml.GSSignatureValue
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.utils.XMLUtils
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.Dereferencer
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.binding.Signature
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.binding.SignatureValue
import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.binding.SimpleProof
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
        const val RSS_ALGORITHM = "GSRSSwithRSAandBPA";
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
            .getKeyPairFromLoggedInUser()
            .keyPair;

        val rss = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        rss.initSign(keyPair);
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addSignSelector(selector, true)

        return rss.sign();
    }

    fun signDocumentWithSeparatedSignature(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        val keyPair = securityService
            .getKeyPairFromLoggedInUser()
            .keyPair;

        rss.initSign(keyPair);
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addSignSelector(selector, true)

        return rss.signSeparate();
    }

    fun redactDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        val publicKey = securityService
            .getKeyPairFromLoggedInUser()
            .publicKey;

        rss.initRedact(publicKey)
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addRedactSelector(selector)

        return rss.redact();
    }

    fun verifyDocument(signedDoc : Document) : Boolean {
        val sig = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        //TODO get public key from document instead of logged in user
        val publicKey = securityService
            .getKeyPairFromLoggedInUser()
            .publicKey;

        sig.initVerify(publicKey);

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
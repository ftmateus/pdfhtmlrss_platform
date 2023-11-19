package pt.unl.fct.di.pdf_html_rss_core.services

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.xml.RedactableXMLSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.w3c.dom.Document

@Service
class RedactableSignaturesService {

    @Autowired
    lateinit var securityService : SecurityService;

    companion object {
        const val RSS_ALGORITHM = "GSRSSwithRSAandBPA";
    }

    fun signAndRedactDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val signedDoc = signDocument(doc, redactSelectors)

        if(redactSelectors.isEmpty())
            return signedDoc

        return redactDocument(signedDoc, redactSelectors);
    }

    fun signDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
//        signSelectors: List<String> = emptyList(),
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        rss.initSign(securityService.keyPair);
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addSignSelector(selector, true)

        return rss.sign();
    }

    fun redactDocument(
        doc: Document,
        redactSelectors: List<String> = emptyList()
    ) : Document {
        val rss = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        rss.initRedact(securityService.publicKey)
        rss.setDocument(doc)

        for (selector in redactSelectors)
            rss.addRedactSelector(selector)

        return rss.redact();
    }

    fun verifyDocument(signedDoc : Document) : Boolean {
        val sig = RedactableXMLSignature.getInstance(RSS_ALGORITHM);

        sig.initVerify(securityService.publicKey);

        sig.setDocument(signedDoc)

        return sig.verify()
    }
}
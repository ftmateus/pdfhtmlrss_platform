package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.stereotype.Service
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper

@Service
class PDFStandardSignatureService {
    fun signDocument(pdfFile : PDFFileWrapper) : PDFFileWrapper {
        throw NotImplementedError();
    }

    fun verifyDocument(pdfFile : PDFFileWrapper) : PDFFileWrapper {
        throw NotImplementedError();
    }
}
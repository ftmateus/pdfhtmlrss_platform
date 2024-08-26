package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcess
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcessAction
import pt.unl.fct.di.pdf_html_rss_core.repositories.RedactionProcessRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@Service
class RedactionProcessService {

    @Autowired
    lateinit var redactionProcessRepository: RedactionProcessRepository;

    @Autowired
    lateinit var temporaryFilesRepository: TemporaryFilesRepository

    @Autowired
    lateinit var fileConversionService: FileConversionService

    @Autowired
    lateinit var domService : DOMService;

    @Autowired
    lateinit var pdfManipulationService: PDFManipulationService;

    @Autowired
    lateinit var compressionService : CompressionService;

    @Autowired
    lateinit var xhtmlRedactableSignatureService: XHTMLRedactableSignatureService

    fun initiatePdfRedactionProcess(
        pdfFile : PDFFileWrapper,
        action : RedactionProcessAction
    ) : RedactionProcess {
        val tmpHtmlFile = temporaryFilesRepository.writeToTempFile { tmpFileOut ->
            val docBytes = fileConversionService
                .generateHTMLFromPDF(pdfFile)
            tmpFileOut.write(docBytes)
        }

        val tmpPdfFile = temporaryFilesRepository.writeToTempFile { tmpFileOut ->
            pdfFile.getInputStream().use {
                it.copyTo(tmpFileOut)
            }
        }

        return RedactionProcess(
            //TODO user id
            userId = "",
            tmpHtmlFile = tmpHtmlFile.name,
            tmpPdfFile = tmpPdfFile.name,
            fileType = MediaType.APPLICATION_PDF.toString(),
            action = action,
        ).also { redactionProcessRepository.save(it) }
    }

    fun initiateXHtmlRedactionProcess(
        //TODO
        htmlFileInputStream : InputStream,
        action : RedactionProcessAction
    ) : RedactionProcess {
        val tmpHtmlFile = temporaryFilesRepository.writeToTempFile { tmpFileOut ->
            htmlFileInputStream.copyTo(tmpFileOut)
            htmlFileInputStream.close()
        }

        return RedactionProcess(
            //TODO user id
            userId = "",
            tmpHtmlFile = tmpHtmlFile.name,
            tmpPdfFile = null,
            fileType = MediaType.TEXT_HTML.toString(),
            action = action
        ).also { redactionProcessRepository.save(it) }
    }

    fun cancelRedactionProcess(
        processId : String
    ) {
        redactionProcessRepository.deleteById(processId)
    }

    fun finalizeRedactionProcess(
        processId : String,
        elementsToRedact : List<String>,
        outputStream : OutputStream,
    ) : RedactionProcess {
        //TODO change all exceptions
        val process = redactionProcessRepository.findById(processId)
            .orElseThrow { RuntimeException() }

        if (process.expires <= System.currentTimeMillis()) {
            redactionProcessRepository.deleteById(processId)
            throw RuntimeException()
        }

        val htmlTmpFile = temporaryFilesRepository.getTempFile(process.tmpHtmlFile)
            ?: throw RuntimeException()

        val htmlTmpDom = htmlTmpFile.inputStream().use {
            domService.parseDocument(it)
        }

        val processedDoc : Document = when(process.action) {
            RedactionProcessAction.SELECT_REDACTABLE_ELEMS ->
                xhtmlRedactableSignatureService
                    .signDocument(htmlTmpDom, elementsToRedact)
            RedactionProcessAction.REDACT ->
                xhtmlRedactableSignatureService
                    .redactDocument(htmlTmpDom, elementsToRedact)
            else -> throw AssertionError("Unknown Redaction Process Action!")
        }

        if(MediaType.parseMediaType(process.fileType) == MediaType.APPLICATION_PDF) {
            val pdf = finalizePdfRedactionProcess(process, processedDoc)
            pdf.getInputStream().use {
                it.copyTo(outputStream)
            }
        }

        domService.writeDocumentToStream(htmlTmpDom, outputStream)

        redactionProcessRepository.delete(process)

        htmlTmpFile.delete()

        return process
    }

    fun getRedactionProcess(processId : String ) : RedactionProcess {
        //Change exception
        return redactionProcessRepository.findById(processId)
            .orElseThrow { throw RuntimeException("Not found!") }
    }

    private fun finalizePdfRedactionProcess(
        process: RedactionProcess,
        processedSignedDoc : Document
    ) : PDFFileWrapper {
        val tmpPdfFile = temporaryFilesRepository.getTempFile(
            process.tmpPdfFile ?: ""
        ) ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)

        val pdf = PDFFileWrapper(tmpPdfFile);

        val compressedHtml = ByteArrayOutputStream().use {
            compressionService.compressGZip(it) { gzipos ->
                domService.writeDocumentToStream(processedSignedDoc, gzipos)
            }
            it.toByteArray()
        }

        val newPdfData = pdfManipulationService.addAttachmentsToPdf(
            pdf, mapOf(
                "rss.html.gz"
                        to compressedHtml.inputStream()
            )
        )

        tmpPdfFile.delete()

        return PDFFileWrapper(
            InputStreamResource(
                newPdfData.inputStream()
            )
        )
    }
}
package pt.unl.fct.di.pdf_html_rss_core.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.w3c.dom.Document
import pt.unl.fct.di.pdf_html_rss_core.dto.PDFFileWrapper
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcess
import pt.unl.fct.di.pdf_html_rss_core.dto.RedactionProcessAction
import pt.unl.fct.di.pdf_html_rss_core.exceptions.PDFHTMLRSSException
import pt.unl.fct.di.pdf_html_rss_core.repositories.RedactionProcessRepository
import pt.unl.fct.di.pdf_html_rss_core.repositories.TemporaryFilesRepository
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@Service
class RedactionProcessService {

    @Autowired
    private lateinit var securityService: SecurityService

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

    @Autowired
    lateinit var userService: UserService;

    @Autowired
    lateinit var pAdESService: PAdESService

    fun initiatePdfRedactionProcess(
        pdfFile : PDFFileWrapper,
        action : RedactionProcessAction
    ) : RedactionProcess {
        val loggedInUser = securityService.getLoggedInUser()
            ?: throw PDFHTMLRSSException()

        val taskId = UUID.randomUUID().toString();

        val tmpHtmlFile = temporaryFilesRepository
            .writeToTempFile("${taskId}.html") { tmpFileOut ->
            val docBytes = fileConversionService
                .generateHTMLFromPDF(pdfFile)
            tmpFileOut.write(docBytes)
        }

        val tmpPdfFile = temporaryFilesRepository
            .writeToTempFile("${taskId}.pdf") { tmpFileOut ->
            pdfFile.getInputStream().use {
                it.copyTo(tmpFileOut)
            }
        }

        return RedactionProcess(
            taskId = taskId,
            userId = loggedInUser.userId,
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
        val loggedInUser = securityService.getLoggedInUser() ?: throw PDFHTMLRSSException()

        val taskId = UUID.randomUUID().toString();

        val tmpHtmlFile = temporaryFilesRepository
            .writeToTempFile("${taskId}.pdf") { tmpFileOut ->
            htmlFileInputStream.copyTo(tmpFileOut)
            htmlFileInputStream.close()
        }

        return RedactionProcess(
            taskId = taskId,
            userId = loggedInUser.userId,
            tmpHtmlFile = tmpHtmlFile.name,
            tmpPdfFile = null,
            fileType = MediaType.TEXT_HTML.toString(),
            action = action
        ).also { redactionProcessRepository.save(it) }
    }

    fun deleteRedactionProcess(
        redactionProcess : RedactionProcess
    ) {
        redactionProcessRepository.deleteById(redactionProcess.taskId)

        temporaryFilesRepository
            .getTempFile(redactionProcess.tmpHtmlFile)
            ?.delete();

        temporaryFilesRepository
            .getTempFile(redactionProcess.tmpPdfFile ?: "")
            ?.delete()
    }

    fun finalizeRedactionProcess(
        processId : String,
        elementsToRedact : List<String>,
        outputStream : OutputStream,
    ) : RedactionProcess {
        //TODO change all exceptions
        val process = getRedactionProcess(processId)

        val htmlTmpFile = temporaryFilesRepository.getTempFile(process.tmpHtmlFile)
            ?: throw PDFHTMLRSSException()

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
        else {
            domService.writeDocumentToStream(htmlTmpDom, outputStream)
        }

        redactionProcessRepository.delete(process)

        htmlTmpFile.delete()

        return process
    }

    /*
     * TODO change exceptions
     */
    fun getRedactionProcess(processId : String ) : RedactionProcess {
        val process = redactionProcessRepository.findById(processId)
            .orElseThrow { throw PDFHTMLRSSException() }

        return process;
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

        val pdfWithRSSAttachment = pdfManipulationService.addAttachmentsToPdf(
            pdf, mapOf(
                "rss.html.gz"
                        to compressedHtml.inputStream()
            )
        ).let { PDFFileWrapper("", it) }

        val signedPdf = ByteArrayOutputStream().use {
            pAdESService.signDocument(pdfWithRSSAttachment, it)
            it.toByteArray()
        }


        tmpPdfFile.delete()

        return PDFFileWrapper(
            InputStreamResource(
                signedPdf.inputStream()
            )
        )
    }
}
package pt.unl.fct.di.pdf_html_rss_core.dto

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import java.io.*

class PDFFileWrapper {

    val resource : Resource

    private var data : ByteArray? = null;
    val name : String;
    private var fetchedMetadata = false;
    var numberOfPages : Int = 0
        get() = getMetadataProperty({ this.numberOfPages }, field)
        private set;

    var fileLength : Long = 0
        get() = getMetadataProperty({ this.fileLength }, field);
        private set;

    var pdfVersion : Char = '0'
        get() = getMetadataProperty({ this.pdfVersion }, field);
        private set;

    constructor(resource: Resource, name: String? = null) {
        this.resource = resource;
        this.name = name ?: resource.filename ?: "";
    }

    constructor(file: File, name: String? = null) {
        this.resource = FileSystemResource(file);
        this.name = name ?: file.nameWithoutExtension;
    }

    constructor(name : String, byteArray: ByteArray) {
        this.resource = ByteArrayResource(byteArray);
        this.name = name
    }

    constructor(name : String, inputStream: InputStream) {
        this.resource = InputStreamResource(inputStream);
        this.name = name
    }

    fun getInputStream() : InputStream {
        return resource.inputStream
    }

    fun getData() : ByteArray {
        return data ?: resource.inputStream.use {
            it.readBytes();
        }
    }

    private fun fetchMetadata() {
        useItextPdfReader {
            this.numberOfPages = it.numberOfPages
            this.pdfVersion = it.pdfVersion
            this.fileLength = it.fileLength
        }

        fetchedMetadata = true;
    }

    fun getPageContent(pageNum : Int) : ByteArray {
        return useItextPdfReader {
            it.getPageContent(pageNum)
        }
    }

    fun toItextPdfReader(): PdfReader = PdfReader(getData().inputStream());

    fun<T> useItextPdfReader(block: (PdfReader) -> T) : T {
        val itextPdfReader = this.toItextPdfReader();
        try {
            return block(toItextPdfReader())
        }
        finally {
            itextPdfReader.close();
        }
    }

    fun<T> useItextPdfStamper(block: (PdfStamper) -> T, outputStream: OutputStream) : T {
        return useItextPdfReader {
            val itextPdfStamper = PdfStamper(it, outputStream);
            try {
                block(itextPdfStamper)
            }
            finally {
                itextPdfStamper.close();
            }
        }
    }

    private fun<T> getMetadataProperty(updateField : () -> T, actualField : T) : T {
        if(!fetchedMetadata) {
            fetchMetadata();
            return updateField();
        }

        return actualField;
    }

    override fun toString() = name
}
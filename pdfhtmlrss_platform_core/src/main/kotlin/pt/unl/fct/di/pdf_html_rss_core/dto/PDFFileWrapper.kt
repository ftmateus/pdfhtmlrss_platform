package pt.unl.fct.di.pdf_html_rss_core.dto

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.io.*

class PDFFileWrapper : Closeable {

    private var inputStream : BufferedInputStream;

    val file : File?;

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

    constructor(file: File, name: String? = null) {
        this.file = file;
        this.name = name ?: file.nameWithoutExtension;

        this.inputStream = BufferedInputStream(FileInputStream(file));
    }

    constructor(name : String, byteArray: ByteArray) {
        this.file = null;
        this.name = name
        this.inputStream = BufferedInputStream(ByteArrayInputStream(byteArray));
    }

    constructor(name : String, inputStream: InputStream) {
        this.file = null;
        this.name = name
        this.inputStream = BufferedInputStream(inputStream);
    }

    fun getData() : ByteArray {
        return data ?: inputStream.readBytes().also {
            data = it;
            inputStream.close()
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

    override fun close() {
        this.inputStream.close()
    }

    override fun toString() = name
}
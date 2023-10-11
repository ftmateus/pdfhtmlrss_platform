package pt.unl.fct.di.pdf_html_rss_core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PDFHTMLRSSApplication

fun main(args: Array<String>) {
	runApplication<PDFHTMLRSSApplication>(*args)
}

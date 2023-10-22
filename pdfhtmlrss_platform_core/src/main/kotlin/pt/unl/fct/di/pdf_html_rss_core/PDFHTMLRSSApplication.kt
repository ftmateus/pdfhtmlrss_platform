package pt.unl.fct.di.pdf_html_rss_core

import de.unipassau.wolfgangpopp.xmlrss.wpprovider.WPProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.security.Security

@SpringBootApplication
class PDFHTMLRSSApplication

fun main(args: Array<String>) {
	Security.addProvider(WPProvider());
	runApplication<PDFHTMLRSSApplication>(*args)
}

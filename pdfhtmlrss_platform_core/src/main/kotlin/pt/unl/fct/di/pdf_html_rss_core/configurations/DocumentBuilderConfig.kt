package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.xml.parsers.DocumentBuilderFactory


@Configuration
class DocumentBuilderConfig {

    @Bean
    fun documentBuilderFactoryFileConversion() : DocumentBuilderFactory =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                isValidating = false
                isNamespaceAware = false
            }

    @Bean
//    @Scope("prototype")
    fun documentBuilderFactoryDefault() : DocumentBuilderFactory =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                //https://github.com/qzind/tray/commit/c04b510515246954a5a26475ae46434b7f127437
//                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//                setFeature("http://xml.org/sax/features/external-general-entities", false);
//                setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                isValidating = true;
                isNamespaceAware = true
                isIgnoringElementContentWhitespace = true
            }
}
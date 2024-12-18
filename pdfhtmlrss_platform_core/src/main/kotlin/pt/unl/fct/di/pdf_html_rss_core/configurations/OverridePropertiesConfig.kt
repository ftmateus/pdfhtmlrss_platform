package pt.unl.fct.di.pdf_html_rss_core.configurations

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource


@Configuration
@PropertySource("classpath:application-override.properties")
class OverridePropertiesConfig {
}
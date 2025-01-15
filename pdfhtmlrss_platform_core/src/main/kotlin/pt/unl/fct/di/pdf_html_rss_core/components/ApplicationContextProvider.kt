package pt.unl.fct.di.pdf_html_rss_core.components

import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.springframework.context.ApplicationContext

@Component
class ApplicationContextProvider : ApplicationContextAware {
    companion object {
        var context: ApplicationContext? = null

        @JvmStatic
        fun <T> getBean(beanClass: Class<T>): T? {
            return context?.getBean(beanClass)
        }

    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}
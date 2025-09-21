package my.stormov.kauthserver.yggdrasil.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class StaticResourcesConfig(private val ygg: YggProperties) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (ygg.textures.serveLocal) {
            val root = Paths.get(ygg.textures.storageDir).toAbsolutePath().normalize().toString()
            registry.addResourceHandler("/textures/**")
                .addResourceLocations("file:$root/")
                .setCachePeriod(31536000)
        }
    }
}
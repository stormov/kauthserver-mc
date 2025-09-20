package my.stormov.kauthserver.yggdrasil

import my.stormov.kauthserver.yggdrasil.config.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(value = [AuthProperties::class, YggProperties::class])
class YggdrasilAuthApplication

fun main(args: Array<String>) {
    runApplication<YggdrasilAuthApplication>(*args)
}
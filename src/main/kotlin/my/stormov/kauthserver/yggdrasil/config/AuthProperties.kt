package my.stormov.kauthserver.yggdrasil.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("auth")
data class AuthProperties(
    val accessTokenTtl: Duration = Duration.ofHours(24),
    val pepper: String = "",
    val joinTtl: Duration = Duration.ofSeconds(60),
    val bcryptStrength: Int = 10
)
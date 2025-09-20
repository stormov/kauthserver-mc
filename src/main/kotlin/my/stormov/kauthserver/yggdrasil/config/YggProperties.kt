package my.stormov.kauthserver.yggdrasil.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ygg")
data class YggProperties(
    val meta: Meta = Meta(),
    val skinDomains: List<String> = emptyList(),
    val signaturePublicKey: String? = null,
    val yggdrasil: Yggdrasil = Yggdrasil(),
    val baseUrl: String? = null
) {
    data class Meta(
        val implementationName: String = "Yggdrasil API",
        val implementationVersion: String? = null,
        val serverName: String = "Server",
        val links: Map<String, String> = emptyMap()
    )
    data class Yggdrasil(
        val api: String? = null
    )
}
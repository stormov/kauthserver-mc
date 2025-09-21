package my.stormov.kauthserver.yggdrasil.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("ygg")
data class YggProperties(
    val meta: Meta = Meta(),
    val skinDomains: List<String> = emptyList(),
    val signaturePublicKey: String? = null,
    val signaturePrivateKey: String? = null,
    val yggdrasil: Yggdrasil = Yggdrasil(),
    val baseUrl: String? = null,
    val services: Services = Services(),
    val textures: Textures = Textures()
) {
    data class Meta(
        val implementationName: String = "Yggdrasil API",
        val implementationVersion: String? = null,
        val serverName: String = "Server",
        val links: Map<String, String> = emptyMap(),
        val feature: Feature = Feature()
    )
    data class Feature(
        val enableProfileKey: Boolean = false
    )
    data class Yggdrasil(val api: String? = null)

    data class Services(
        val attributes: AttributesDefaults = AttributesDefaults(),
        val profileKeys: ProfileKeys = ProfileKeys(),
        val blocklistEnabled: Boolean = true
    )
    data class AttributesDefaults(
        val onlineChat: Boolean = true,
        val multiplayerServer: Boolean = true,
        val multiplayerRealms: Boolean = true,
        val telemetry: Boolean = false
    )
    data class ProfileKeys(
        val enabled: Boolean = false,
        val ttl: Duration = Duration.ofHours(48),
        val signingPrivateKey: String? = null
    )
    data class Textures(
        val sign: Boolean = false,
        val serveLocal: Boolean = false,
        val storageDir: String = "./data/textures",
        val baseUrl: String? = null
    )
}
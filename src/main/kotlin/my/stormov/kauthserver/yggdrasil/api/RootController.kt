package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.config.YggProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController(
    private val props: YggProperties
) {
    data class MetaOut(
        val implementationName: String,
        val implementationVersion: String,
        val serverName: String,
        val links: Map<String, String>
    )
    data class YggdrasilOut(val api: String?)
    data class RootOut(
        val meta: MetaOut,
        val skinDomains: List<String>,
        val signaturePublicKey: String?,
        val yggdrasil: YggdrasilOut,
        val baseUrl: String?
    )

    @GetMapping("/")
    fun root(): RootOut {
        val version = props.meta.implementationVersion
            ?: (this::class.java.`package`.implementationVersion ?: "unknown")
        return RootOut(
            meta = MetaOut(
                implementationName = props.meta.implementationName,
                implementationVersion = version,
                serverName = props.meta.serverName,
                links = props.meta.links
            ),
            skinDomains = props.skinDomains,
            signaturePublicKey = props.signaturePublicKey,
            yggdrasil = YggdrasilOut(api = props.yggdrasil.api),
            baseUrl = props.baseUrl
        )
    }
}
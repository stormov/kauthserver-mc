package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.config.*
import my.stormov.kauthserver.yggdrasil.service.*
import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/minecraftservices")
class MinecraftServicesController(
    private val props: YggProperties,
    private val auth: AuthService,
    private val keys: ProfileKeyService,
    private val privacy: PrivacyService
) {
    private fun principalUserId(authHeader: String?): java.util.UUID? {
        val token = authHeader?.removePrefix("Bearer ")?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val session = auth.findActiveByAccessToken(token) ?: return null
        return session.user.id
    }

    @GetMapping("/player/attributes")
    fun attributes(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): AttributesResponse {
        val p = props.services.attributes
        return AttributesResponse(
            privileges = Privileges(
                onlineChat = PrivilegeFlag(p.onlineChat),
                multiplayerServer = PrivilegeFlag(p.multiplayerServer),
                multiplayerRealms = PrivilegeFlag(p.multiplayerRealms),
                telemetry = PrivilegeFlag(p.telemetry)
            )
        )
    }

    @GetMapping("/privacy/blocklist")
    fun blocklist(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): BlocklistResponse {
        val userId = principalUserId(authHeader) ?: return BlocklistResponse()
        return privacy.blocklistFor(userId)
    }

    @RequestMapping("/player/certificates", method = [RequestMethod.GET, RequestMethod.POST])
    fun certificates(
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<PlayerCertificatesResponse> {
        if (!props.services.profileKeys.enabled) {
            return ResponseEntity.status(404).build()
        }
        val userId = principalUserId(authHeader) ?: return ResponseEntity.status(401).build()
        val now = Instant.now()
        val resp = keys.issueFor(userId, now)
        return ResponseEntity.ok(resp)
    }
}
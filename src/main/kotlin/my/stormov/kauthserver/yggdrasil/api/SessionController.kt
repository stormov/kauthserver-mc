package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.api.dto.request.*
import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.service.SessionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sessionserver/session/minecraft")
class SessionController(
    private val sessionService: SessionService
) {

    @PostMapping("/join")
    fun join(@RequestBody req: JoinRequest): ResponseEntity<Void> {
        sessionService.join(req.accessToken, req.selectedProfile, req.serverId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/hasJoined")
    fun hasJoined(
        @RequestParam username: String,
        @RequestParam serverId: String,
        @RequestParam(required = false) ip: String?
    ): ResponseEntity<ProfileResponse> {
        val profile = sessionService.hasJoined(username, serverId, ip)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(profile)
    }

    @GetMapping("/profile/{uuid}")
    fun profile(
        @PathVariable uuid: String,
        @RequestParam(defaultValue = "false") unsigned: Boolean
    ): ResponseEntity<ProfileResponse> {
        val p = sessionService.profile(uuid, unsigned)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(p)
    }
}
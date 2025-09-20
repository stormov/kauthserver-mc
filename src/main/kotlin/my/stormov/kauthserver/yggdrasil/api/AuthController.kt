package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.api.dto.request.*
import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/authserver")
class AuthController(private val auth: AuthService) {

    @PostMapping("/authenticate")
    fun authenticate(@RequestBody req: AuthenticateRequest): AuthenticateResponse =
        auth.authenticate(req)

    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): RefreshResponse =
        auth.refresh(req)

    @PostMapping("/validate")
    fun validate(@RequestBody req: ValidateRequest): ResponseEntity<Void> {
        auth.validate(req)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/invalidate")
    fun invalidate(@RequestBody req: InvalidateRequest): ResponseEntity<Void> {
        auth.invalidate(req)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/signout")
    fun signout(@RequestBody req: SignoutRequest): ResponseEntity<Void> {
        auth.signout(req)
        return ResponseEntity.noContent().build()
    }
}
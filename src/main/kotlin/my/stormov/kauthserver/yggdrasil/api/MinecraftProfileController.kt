package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.domain.ProfileEntity
import my.stormov.kauthserver.yggdrasil.repository.*
import my.stormov.kauthserver.yggdrasil.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import javax.imageio.ImageIO

@RestController
@RequestMapping("/minecraftservices/minecraft/profile")
class MinecraftProfileController(
    private val auth: AuthService,
    private val profiles: ProfileRepository,
    private val profileTextures: ProfileTextureRepository,
    private val storage: TextureStorageService,
    private val textures: ProfileTextureService
) {
    data class SkinView(
        val id: String,
        val state: String = "ACTIVE",
        val url: String,
        val variant: String,
        val alias: String? = null
    )
    data class CapeView(
        val id: String,
        val state: String = "ACTIVE",
        val url: String,
        val alias: String? = null
    )
    data class ProfileView(
        val id: String,
        val name: String,
        val skins: List<SkinView> = emptyList(),
        val capes: List<CapeView> = emptyList()
    )

    @GetMapping
    fun getProfile(@RequestHeader("Authorization", required = false) authHeader: String?): ResponseEntity<ProfileView> {
        val (profile) = resolveCurrentProfile(authHeader)
        val t = profileTextures.findByProfileId(profile.id!!)
        val skins = buildList {
            t?.skinUrl?.let { url ->
                val variant = normalizeVariant(t.skinModel)
                add(SkinView(
                    id = sha1Hex(url),
                    url = url,
                    variant = variant,
                    alias = if (variant == "slim") "ALEX" else "STEVE"
                ))
            }
        }
        val capes = buildList {
            t?.capeUrl?.let { url ->
                add(CapeView(
                    id = sha1Hex(url),
                    url = url
                ))
            }
        }
        return ResponseEntity.ok(ProfileView(
            id = noDash(profile.id!!),
            name = profile.name,
            skins = skins,
            capes = capes
        ))
    }

    @PostMapping(
        value = ["/skins"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadSkinMultipart(
        @RequestParam("file", required = false) file: MultipartFile?,
        @RequestParam("skin", required = false) fileAlt: MultipartFile?,
        @RequestParam(name = "variant", required = false) variant: String?,
        @RequestParam(name = "model", required = false) model: String?,
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        val selected = file ?: fileAlt
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing file")
        if (selected.contentType != MediaType.IMAGE_PNG_VALUE) {
            throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image/png is accepted")
        }
        val (profile) = resolveCurrentProfile(authHeader)
        val bytes = selected.bytes
        validatePngDimensions(bytes)
        val url = storage.storeSkin(bytes)
        textures.setSkin(profile, url, chooseVariant(model, variant))
        return ResponseEntity.noContent().build() // 204 — как у Mojang
    }

    // Загрузка по URL (x-www-form-urlencoded)
    @PostMapping(
        value = ["/skins"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun uploadSkinByUrl(
        @RequestParam("url") url: String,
        @RequestParam(name = "variant", required = false) variant: String?,
        @RequestParam(name = "model", required = false) model: String?,
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        val (profile) = resolveCurrentProfile(authHeader)
        val png = downloadPng(url)
        validatePngDimensions(png)
        val stored = storage.storeSkin(png)
        textures.setSkin(profile, stored, chooseVariant(model, variant))
        return ResponseEntity.noContent().build()
    }

    private fun resolveCurrentProfile(authHeader: String?): Pair<ProfileEntity, UUID> {
        val token = authHeader?.removePrefix("Bearer ")?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token")
        val session = auth.findActiveByAccessToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        val prof = session.profile ?: profiles.findByUserId(session.user.id!!).firstOrNull()
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No profiles for user")
        return prof to session.user.id!!
    }

    private fun chooseVariant(model: String?, variant: String?): String {
        val v = (model ?: variant)?.lowercase()
        return when (v) {
            "slim", "alex" -> "slim"
            "classic", "steve", null, "" -> "classic"
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid variant: $v")
        }
    }

    private fun validatePngDimensions(bytes: ByteArray) {
        val img = ImageIO.read(bytes.inputStream())
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid PNG")
        val w = img.width
        val h = img.height
        val okRatio = (w == h) || (w == 2 * h)
        val minOk = (w >= 64 && h >= 32)
        val maxOk = (w <= 4096 && h <= 4096)
        val multiples = (w % 64 == 0) && (h % 32 == 0)
        if (!(okRatio && minOk && maxOk && multiples)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported skin size ${w}x$h. Allowed: multiples of 64x64 or 64x32 up to 4096."
            )
        }
    }

    private fun downloadPng(url: String): ByteArray {
        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
        val req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofByteArray())
        if (res.statusCode() !in 200..299) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to download skin: HTTP ${res.statusCode()}")
        }
        val body = res.body()
        val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        if (body.size < 8 || !body.take(8).toByteArray().contentEquals(sig)) {
            throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Downloaded file is not PNG")
        }
        return body
    }

    private fun normalizeVariant(model: String?): String =
        when (model?.lowercase()) {
            "slim", "alex" -> "slim"
            "classic", "steve", null, "" -> "classic"
            else -> "classic"
        }

    private fun noDash(u: UUID) = u.toString().replace("-", "")
    private fun sha1Hex(s: String): String =
        java.security.MessageDigest.getInstance("SHA-1")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
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
@RequestMapping("/api/user/profile")
class UserProfileController(
    private val auth: AuthService,
    private val profiles: ProfileRepository,
    private val storage: TextureStorageService,
    private val textures: ProfileTextureService
) {

    @PutMapping(
        value = ["/{uuid}/skin"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadSkinPut(
        @PathVariable uuid: String,
        @RequestParam("file", required = false) file: MultipartFile?,
        @RequestParam("skin", required = false) fileAlt: MultipartFile?,
        @RequestParam(name = "model", required = false) model: String?,
        @RequestParam(name = "variant", required = false) variant: String?,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        val f = file ?: fileAlt
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing file")
        processUpload(uuid, authHeader) { bytes ->
            if (f.contentType != MediaType.IMAGE_PNG_VALUE) {
                throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image/png is accepted")
            }
            bytes(f.bytes)
            model ?: variant
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping(
        value = ["/{uuid}/skin"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadSkinPostMultipart(
        @PathVariable uuid: String,
        @RequestParam("file", required = false) file: MultipartFile?,
        @RequestParam("skin", required = false) fileAlt: MultipartFile?,
        @RequestParam(name = "model", required = false) model: String?,
        @RequestParam(name = "variant", required = false) variant: String?,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        val f = file ?: fileAlt
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing file")
        processUpload(uuid, authHeader) { bytes ->
            if (f.contentType != MediaType.IMAGE_PNG_VALUE) {
                throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only image/png is accepted")
            }
            bytes(f.bytes)
            model ?: variant
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping(
        value = ["/{uuid}/skin"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun uploadSkinPostByUrl(
        @PathVariable uuid: String,
        @RequestParam("url") url: String,
        @RequestParam(name = "model", required = false) model: String?,
        @RequestParam(name = "variant", required = false) variant: String?,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        processUpload(uuid, authHeader) { bytes ->
            val png = downloadPng(url)
            bytes(png)
            model ?: variant
        }
        return ResponseEntity.noContent().build()
    }

    // DELETE — сброс
    @DeleteMapping("/{uuid}/skin")
    fun deleteSkin(
        @PathVariable uuid: String,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<Void> {
        val (profile) = checkOwnership(uuid, authHeader)
        textures.setSkin(profile, skinUrl = "", model = "classic")
        return ResponseEntity.noContent().build()
    }

    private fun processUpload(
        uuidStr: String,
        authHeader: String?,
        build: (bytes: (ByteArray) -> Unit) -> String?
    ) {
        val (profile) = checkOwnership(uuidStr, authHeader)
        var bytes: ByteArray? = null
        val model = build { b -> bytes = b }
        val data = bytes ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No data")
        validatePngDimensions(data)
        val url = storage.storeSkin(data)
        textures.setSkin(profile, url, model)
    }

    private fun checkOwnership(uuidStr: String, authHeader: String?): Pair<ProfileEntity, UUID> {
        val token = bearer(authHeader) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token")
        val session = auth.findActiveByAccessToken(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        val profileId = parseUuidFlexible(uuidStr)
        val profile = profiles.findById(profileId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found")
        }
        if (profile.user.id != session.user.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Profile does not belong to authenticated user")
        }
        return profile to session.user.id!!
    }

    private fun bearer(header: String?) =
        header?.trim()?.removePrefix("Bearer ")?.takeIf { it.isNotBlank() }

    private fun parseUuidFlexible(s: String): UUID =
        if (s.contains("-")) UUID.fromString(s)
        else UUID.fromString(s.replace(Regex("(.{8})(.{4})(.{4})(.{4})(.{12})"), "$1-$2-$3-$4-$5"))

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
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Unsupported skin size ${w}x$h. Allowed: multiples of 64x64 or 64x32 up to 4096.")
        }
    }

    private fun downloadPng(url: String): ByteArray {
        val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
        val req = HttpRequest.newBuilder(URI.create(url)).GET().timeout(Duration.ofSeconds(10)).build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofByteArray())
        if (res.statusCode() !in 200..299) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to download skin: HTTP ${res.statusCode()}")
        }
        val body = res.body()
        val pngSig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        if (body.size < 8 || !body.take(8).toByteArray().contentEquals(pngSig)) {
            throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Downloaded file is not PNG")
        }
        return body
    }
}
package my.stormov.kauthserver.yggdrasil.service

import com.fasterxml.jackson.databind.ObjectMapper
import my.stormov.kauthserver.yggdrasil.api.dto.session.*
import my.stormov.kauthserver.yggdrasil.config.YggProperties
import my.stormov.kauthserver.yggdrasil.domain.*
import my.stormov.kauthserver.yggdrasil.extensions.noDash
import my.stormov.kauthserver.yggdrasil.repository.*
import org.springframework.stereotype.Service
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*
import java.util.Base64

@Service
class TextureService(
    private val textures: ProfileTextureRepository,
    private val om: ObjectMapper,
    private val ygg: YggProperties
) {
    fun propertiesFor(profile: ProfileEntity, unsigned: Boolean): List<ProfileProperty> {
        val t = textures.findByProfileId(profile.id!!)
        val node = mutableMapOf<String, Any>(
            "timestamp" to Instant.now().toEpochMilli(),
            "profileId" to profile.id!!.noDash,
            "profileName" to profile.name,
            "textures" to linkedMapOf<String, Any>()
        )
        val texturesNode = node["textures"] as LinkedHashMap<String, Any>
        t?.skinUrl?.let { url ->
            val skin = linkedMapOf<String, Any>("url" to url)
            t.skinModel?.let { skin["metadata"] = mapOf("model" to it) }
            texturesNode["SKIN"] = skin
        }
        t?.capeUrl?.let { url -> texturesNode["CAPE"] = mapOf("url" to url) }

        val json = om.writeValueAsBytes(node)
        val value = Base64.getEncoder().encodeToString(json)

        if (unsigned) return listOf(ProfileProperty(name = "textures", value = value))

        val signature = if (ygg.textures.sign && !ygg.signaturePrivateKey.isNullOrBlank()) {
            val priv = loadPrivateFromPem(ygg.signaturePrivateKey)
            val sig = Signature.getInstance("SHA1withRSA").apply {
                initSign(priv); update(value.toByteArray(Charsets.UTF_8))
            }.sign()
            Base64.getEncoder().encodeToString(sig)
        } else null

        return listOf(ProfileProperty(name = "textures", value = value, signature = signature))
    }

    private fun loadPrivateFromPem(pem: String): PrivateKey {
        val content = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(content)
        return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
    }
}
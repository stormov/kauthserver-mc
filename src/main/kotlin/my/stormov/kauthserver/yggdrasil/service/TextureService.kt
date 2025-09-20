package my.stormov.kauthserver.yggdrasil.service

import com.fasterxml.jackson.databind.ObjectMapper
import my.stormov.kauthserver.yggdrasil.api.dto.session.*
import my.stormov.kauthserver.yggdrasil.domain.*
import my.stormov.kauthserver.yggdrasil.repository.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.Base64

@Service
class TextureService(
    private val textures: ProfileTextureRepository,
    private val om: ObjectMapper
) {
    private fun uuidNoDash(u: UUID) = u.toString().replace("-", "")

    fun propertiesFor(profile: ProfileEntity, unsigned: Boolean): List<ProfileProperty> {
        val t = textures.findByProfileId(profile.id!!)
        val node = mutableMapOf<String, Any>(
            "timestamp" to Instant.now().toEpochMilli(),
            "profileId" to uuidNoDash(profile.id!!),
            "profileName" to profile.name,
            "textures" to linkedMapOf<String, Any>()
        )

        val texturesNode = node["textures"] as LinkedHashMap<String, Any>
        t?.skinUrl?.let { url ->
            val skin = linkedMapOf<String, Any>("url" to url)
            t.skinModel?.let { skin["metadata"] = mapOf("model" to it) }
            texturesNode["SKIN"] = skin
        }
        t?.capeUrl?.let { url ->
            texturesNode["CAPE"] = mapOf("url" to url)
        }

        val json = om.writeValueAsBytes(node)
        val value = Base64.getEncoder().encodeToString(json)

        return listOf(ProfileProperty(name = "textures", value = value, signature = null))
    }
}
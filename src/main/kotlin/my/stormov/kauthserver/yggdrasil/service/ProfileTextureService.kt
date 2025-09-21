package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.domain.*
import my.stormov.kauthserver.yggdrasil.repository.ProfileTextureRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ProfileTextureService(private val repo: ProfileTextureRepository) {

    @Transactional
    fun setSkin(profile: ProfileEntity, skinUrl: String, model: String?): ProfileTextureEntity {
        val current = repo.findByProfileId(profile.id!!)
        val m = when (model?.lowercase()) {
            null, "", "classic" -> "classic"
            "slim", "alex" -> "slim"
            else -> throw IllegalArgumentException("Invalid model: $model (use classic|slim)")
        }
        val updated = (current ?: ProfileTextureEntity(profile = profile)).apply {
            this.skinUrl = skinUrl
            this.skinModel = m
            this.updatedAt = Instant.now()
        }
        return repo.save(updated)
    }
}
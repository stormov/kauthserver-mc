package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.ProfileTextureEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProfileTextureRepository : JpaRepository<ProfileTextureEntity, UUID> {
    fun findByProfileId(profileId: UUID): ProfileTextureEntity?
}
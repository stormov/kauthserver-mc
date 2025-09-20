package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.ProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProfileRepository : JpaRepository<ProfileEntity, UUID> {
    fun findByUserId(userId: UUID): List<ProfileEntity>
    fun findByNameIgnoreCase(name: String): ProfileEntity?
}
package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.PrivacyBlocklistEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PrivacyBlocklistRepository : JpaRepository<PrivacyBlocklistEntity, UUID> {
    fun findAllByUserId(userId: UUID): List<PrivacyBlocklistEntity>
}
package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.SessionTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface SessionTokenRepository : JpaRepository<SessionTokenEntity, UUID> {
    fun findByAccessTokenHashAndRevokedFalse(accessTokenHash: String): SessionTokenEntity?
    fun deleteByUserId(userId: UUID)
    fun findAllByUserIdAndRevokedFalse(userId: UUID): List<SessionTokenEntity>
    fun deleteAllByExpiresAtBefore(instant: Instant): Long
}
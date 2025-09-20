package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.ServerJoinEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface ServerJoinRepository : JpaRepository<ServerJoinEntity, UUID> {
    fun findTopByProfileIdAndServerIdAndExpiresAtAfterAndUsedFalse(
        profileId: UUID,
        serverId: String,
        now: Instant
    ): ServerJoinEntity?

    fun deleteAllByExpiresAtBefore(now: Instant): Long
}
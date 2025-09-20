package my.stormov.kauthserver.yggdrasil.repository

import my.stormov.kauthserver.yggdrasil.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByUsernameIgnoreCase(username: String): UserEntity?
}
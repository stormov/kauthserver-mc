package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "privacy_blocklist")
class PrivacyBlocklistEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    var blockedUser: UserEntity
)
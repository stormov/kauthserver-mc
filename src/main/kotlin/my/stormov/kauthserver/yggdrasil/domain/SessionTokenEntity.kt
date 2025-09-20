package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "session_tokens",
    indexes = [Index(name = "idx_access_token_hash", columnList = "access_token_hash", unique = true)]
)
class SessionTokenEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    var profile: ProfileEntity? = null,

    @Column(name = "access_token_hash", nullable = false, unique = true, length = 64)
    var accessTokenHash: String,

    @Column(name = "client_token", nullable = false, length = 64)
    var clientToken: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var revoked: Boolean = false,

    @Column(name = "last_seen_at", nullable = false)
    var lastSeenAt: Instant = Instant.now()
)
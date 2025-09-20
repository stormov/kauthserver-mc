package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "server_joins",
    indexes = [Index(name = "idx_server_joins_profile_server", columnList = "profile_id, server_id")]
)
class ServerJoinEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    var profile: ProfileEntity,

    @Column(name = "server_id", nullable = false, length = 64)
    var serverId: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(nullable = false)
    var used: Boolean = false
)
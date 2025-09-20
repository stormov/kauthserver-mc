package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 64)
    var username: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var profiles: MutableList<ProfileEntity> = mutableListOf()
}
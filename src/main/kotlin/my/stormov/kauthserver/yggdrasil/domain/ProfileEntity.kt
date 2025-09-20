package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "profiles")
class ProfileEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true, length = 16)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity
)
package my.stormov.kauthserver.yggdrasil.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "profile_textures")
class ProfileTextureEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    var profile: ProfileEntity,

    @Column(name = "skin_url")
    var skinUrl: String? = null,

    @Column(name = "skin_model", length = 8)
    var skinModel: String? = null,

    @Column(name = "cape_url")
    var capeUrl: String? = null,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
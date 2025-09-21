package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.BlockedProfile
import my.stormov.kauthserver.yggdrasil.api.dto.response.BlocklistResponse
import my.stormov.kauthserver.yggdrasil.extensions.noDash
import my.stormov.kauthserver.yggdrasil.repository.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class PrivacyService(
    private val repo: PrivacyBlocklistRepository,
    private val profiles: ProfileRepository
) {
    fun blocklistFor(userId: UUID): BlocklistResponse {
        val entries = repo.findAllByUserId(userId)
        if (entries.isEmpty()) return BlocklistResponse()
        val out = entries.mapNotNull { e ->
            val prof = profiles.findByUserId(e.blockedUser.id!!).firstOrNull()
            prof?.let { BlockedProfile(id = it.id!!.noDash, name = it.name) }
        }
        return BlocklistResponse(out)
    }
}
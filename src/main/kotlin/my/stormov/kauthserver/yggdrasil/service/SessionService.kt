package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.config.AuthProperties
import my.stormov.kauthserver.yggdrasil.domain.ServerJoinEntity
import my.stormov.kauthserver.yggdrasil.extensions.noDash
import my.stormov.kauthserver.yggdrasil.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.*

@Service
class SessionService(
    private val authService: AuthService,
    private val profiles: ProfileRepository,
    private val joins: ServerJoinRepository,
    private val textures: TextureService,
    private val props: AuthProperties
) {

    private fun parseUuidFlexible(s: String): UUID {
        return if (s.contains("-")) UUID.fromString(s)
        else UUID.fromString(s.replace(
            Regex("(.{8})(.{4})(.{4})(.{4})(.{12})"),
            "$1-$2-$3-$4-$5"
        ))
    }

    @Transactional
    fun join(accessToken: String, selectedProfile: String, serverId: String) {
        val session = authService.findActiveByAccessToken(accessToken)
            ?: forbidden("Invalid token.")
        val profileUuid = parseUuidFlexible(selectedProfile)
        val profile = profiles.findById(profileUuid).orElseThrow {
            forbidden("Selected profile does not exist.")
        }
        if (profile.user.id != session.user.id) {
            forbidden("Selected profile does not belong to token owner.")
        }

        session.profile = profile

        val now = Instant.now()
        val expires = now.plus(props.joinTtl)
        joins.save(ServerJoinEntity(profile = profile, serverId = serverId, expiresAt = expires))
    }

    @Transactional
    fun hasJoined(username: String, serverId: String, ip: String?): ProfileResponse? {
        val profile = profiles.findByNameIgnoreCase(username) ?: return null
        val join = joins.findTopByProfileIdAndServerIdAndExpiresAtAfterAndUsedFalse(
            profile.id!!, serverId, Instant.now()
        ) ?: return null

        join.used = true
        joins.save(join)

        val props = textures.propertiesFor(profile, unsigned = true)
        return ProfileResponse(
            id = profile.id!!.toString().replace("-", ""),
            name = profile.name,
            properties = props
        )
    }

    @Transactional(readOnly = true)
    fun profile(uuidStr: String, unsigned: Boolean): ProfileResponse? {
        val uuid = parseUuidFlexible(uuidStr)
        val profile = profiles.findById(uuid).orElse(null) ?: return null
        val props = textures.propertiesFor(profile, unsigned = unsigned)
        return ProfileResponse(
            id = profile.id!!.noDash,
            name = profile.name,
            properties = props
        )
    }

    private fun forbidden(msg: String): Nothing {
        throw ResponseStatusException(HttpStatus.FORBIDDEN, msg)
    }
}
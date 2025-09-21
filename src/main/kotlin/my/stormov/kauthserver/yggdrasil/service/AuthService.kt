package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.api.dto.request.*
import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.api.dto.*
import my.stormov.kauthserver.yggdrasil.config.*
import my.stormov.kauthserver.yggdrasil.domain.*
import my.stormov.kauthserver.yggdrasil.extensions.noDash
import my.stormov.kauthserver.yggdrasil.repository.*
import org.springframework.cache.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val users: UserRepository,
    private val profiles: ProfileRepository,
    private val sessions: SessionTokenRepository,
    private val encoder: PasswordEncoder,
    private val tokens: TokenGenerator,
    private val hasher: TokenHasher,
    private val props: AuthProperties
) {

    @Transactional
    fun authenticate(req: AuthenticateRequest): AuthenticateResponse {
        val user = users.findByUsernameIgnoreCase(req.username)
            ?: forbidden("Invalid credentials. Invalid username or password.")
        if (!encoder.matches(req.password, user.passwordHash)) {
            forbidden("Invalid credentials. Invalid username or password.")
        }

        val clientToken = req.clientToken ?: tokens.clientToken()
        val accessToken = tokens.accessToken()
        val accessHash = hasher.hash(accessToken)

        val userProfiles = profiles.findByUserId(user.id!!)
        val selected = userProfiles.firstOrNull()
        val expiresAt = Instant.now().plus(props.accessTokenTtl)

        sessions.save(
            SessionTokenEntity(
                user = user,
                profile = selected,
                accessTokenHash = accessHash,
                clientToken = clientToken,
                expiresAt = expiresAt
            )
        )

        val gp = userProfiles.map { GameProfile(id = it.id!!.noDash, name = it.name) }
        val selectedGp = selected?.let { GameProfile(id = it.id!!.noDash, name = it.name) }

        return AuthenticateResponse(
            accessToken = accessToken,
            clientToken = clientToken,
            availableProfiles = gp,
            selectedProfile = selectedGp,
            user = if (req.requestUser == true) UserInfo(id = user.id!!.noDash) else null
        )
    }

    @Transactional
    fun refresh(req: RefreshRequest): RefreshResponse {
        val accessHash = hasher.hash(req.accessToken)
        val session = sessions.findByAccessTokenHashAndRevokedFalse(accessHash)
            ?: forbidden("Invalid token.")
        if (req.clientToken != null && session.clientToken != req.clientToken) {
            forbidden("Client token mismatch.")
        }

        val newToken = tokens.accessToken()
        val newHash = hasher.hash(newToken)
        session.accessTokenHash = newHash
        session.expiresAt = Instant.now().plus(props.accessTokenTtl)
        session.lastSeenAt = Instant.now()
        sessions.save(session)
        evictTokenCache(accessHash)

        val selectedGp = session.profile?.let { GameProfile(id = it.id!!.noDash, name = it.name) }

        if (req.selectedProfile != null) {
            val profUuid = UUID.fromString(req.selectedProfile.id.chunked(8).joinToString("-") { it })
            val prof = profiles.findById(profUuid).orElse(null)
            if (prof == null || prof.user.id != session.user.id) {
                forbidden("Selected profile does not belong to token owner.")
            } else {
                session.profile = prof
            }
        }

        return RefreshResponse(
            accessToken = newToken,
            clientToken = session.clientToken,
            selectedProfile = selectedGp,
            user = if (req.requestUser == true) UserInfo(id = session.user.id!!.noDash) else null
        )
    }

    @Transactional(readOnly = true)
    fun validate(req: ValidateRequest) {
        val session = findActiveByAccessToken(req.accessToken)
            ?: forbidden("Invalid token.")
        if (req.clientToken != null && session.clientToken != req.clientToken) {
            forbidden("Client token mismatch.")
        }
    }

    @Transactional
    fun invalidate(req: InvalidateRequest) {
        val accessHash = hasher.hash(req.accessToken)
        val session = sessions.findByAccessTokenHashAndRevokedFalse(accessHash)
            ?: return
        session.revoked = true
        sessions.save(session)
        evictTokenCache(accessHash)
    }

    @Transactional
    fun signout(req: SignoutRequest) {
        val user = users.findByUsernameIgnoreCase(req.username)
            ?: forbidden("Invalid credentials. Invalid username or password.")
        if (!encoder.matches(req.password, user.passwordHash)) {
            forbidden("Invalid credentials. Invalid username or password.")
        }
        val list = sessions.findAllByUserIdAndRevokedFalse(user.id!!)
        list.forEach {
            it.revoked = true
        }
        sessions.saveAll(list)
    }

    @Cacheable(cacheNames = ["tokens"], key = "#accessHash")
    fun findActiveByAccessHashCached(accessHash: String) =
        sessions.findByAccessTokenHashAndRevokedFalse(accessHash)
            ?.takeIf { it.expiresAt.isAfter(Instant.now()) }

    fun findActiveByAccessToken(accessToken: String) =
        findActiveByAccessHashCached(hasher.hash(accessToken))

    @CacheEvict(cacheNames = ["tokens"], key = "#accessHash")
    fun evictTokenCache(accessHash: String) { }

    private fun forbidden(message: String): Nothing {
        throw ResponseStatusException(HttpStatus.FORBIDDEN, message)
    }
}
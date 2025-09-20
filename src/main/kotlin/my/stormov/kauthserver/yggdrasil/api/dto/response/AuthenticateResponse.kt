package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.*

data class AuthenticateResponse(
    val accessToken: String,
    val clientToken: String,
    val availableProfiles: List<GameProfile>,
    val selectedProfile: GameProfile?,
    val user: UserInfo? = null
)
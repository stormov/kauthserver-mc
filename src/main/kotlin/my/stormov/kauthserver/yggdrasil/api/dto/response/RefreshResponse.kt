package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.*

data class RefreshResponse(
    val accessToken: String,
    val clientToken: String,
    val selectedProfile: GameProfile?,
    val user: UserInfo? = null
)
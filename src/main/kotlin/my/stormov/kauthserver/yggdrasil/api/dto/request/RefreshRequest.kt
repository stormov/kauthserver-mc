package my.stormov.kauthserver.yggdrasil.api.dto.request

import my.stormov.kauthserver.yggdrasil.api.dto.GameProfile

data class RefreshRequest(
    val accessToken: String,
    val clientToken: String? = null,
    val requestUser: Boolean? = null,
    val selectedProfile: GameProfile? = null
)
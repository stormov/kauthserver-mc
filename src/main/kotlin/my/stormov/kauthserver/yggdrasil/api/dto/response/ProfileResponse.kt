package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.session.ProfileProperty

data class ProfileResponse(
    val id: String,
    val name: String,
    val properties: List<ProfileProperty> = emptyList()
)
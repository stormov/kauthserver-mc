package my.stormov.kauthserver.yggdrasil.api.dto.request

data class InvalidateRequest(
    val accessToken: String,
    val clientToken: String? = null
)
package my.stormov.kauthserver.yggdrasil.api.dto.request

data class ValidateRequest(
    val accessToken: String,
    val clientToken: String? = null
)
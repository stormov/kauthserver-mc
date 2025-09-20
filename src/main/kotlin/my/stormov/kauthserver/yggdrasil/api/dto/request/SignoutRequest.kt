package my.stormov.kauthserver.yggdrasil.api.dto.request

data class SignoutRequest(
    val username: String,
    val password: String
)
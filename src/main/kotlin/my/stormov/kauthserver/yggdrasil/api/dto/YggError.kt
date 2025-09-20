package my.stormov.kauthserver.yggdrasil.api.dto

data class YggError(
    val error: String,
    val errorMessage: String,
    val cause: String? = null
)
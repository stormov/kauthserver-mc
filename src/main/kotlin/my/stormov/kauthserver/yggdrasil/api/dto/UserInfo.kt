package my.stormov.kauthserver.yggdrasil.api.dto

data class UserInfo(
    val id: String,
    val properties: List<Map<String, String>> = emptyList()
)
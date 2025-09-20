package my.stormov.kauthserver.yggdrasil.api.dto.request

data class JoinRequest(
    val accessToken: String,
    val selectedProfile: String,
    val serverId: String
)
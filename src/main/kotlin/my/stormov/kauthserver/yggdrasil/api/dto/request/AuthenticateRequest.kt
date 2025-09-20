package my.stormov.kauthserver.yggdrasil.api.dto.request

import my.stormov.kauthserver.yggdrasil.api.dto.Agent

data class AuthenticateRequest(
    val agent: Agent? = null,
    val username: String,
    val password: String,
    val clientToken: String? = null,
    val requestUser: Boolean? = null
)
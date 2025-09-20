package my.stormov.kauthserver.yggdrasil.api.dto.session

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProfileProperty(
    val name: String,
    val value: String,
    val signature: String? = null
)
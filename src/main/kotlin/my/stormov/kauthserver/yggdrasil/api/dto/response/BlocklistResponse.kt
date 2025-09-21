package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.BlockedProfile

data class BlocklistResponse(val blockedProfiles: List<BlockedProfile> = emptyList())
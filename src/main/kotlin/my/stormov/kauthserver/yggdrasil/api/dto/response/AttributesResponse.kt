package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.*

/**
 * Data class representing the response containing player attributes.
 *
 * @property privileges The privileges associated with the player.
 * @property profanityFilterPreferences The player's preferences for the profanity filter. Defaults to an empty configuration.
 * @property banStatus The ban status of the player. Defaults to no ban.
 */

data class AttributesResponse(
    val privileges: Privileges,
    val profanityFilterPreferences: ProfanityFilterPreferences = ProfanityFilterPreferences(),
    val banStatus: BanStatus = BanStatus()
)
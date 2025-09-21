package my.stormov.kauthserver.yggdrasil.api.dto.response

import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.KeyPairOut

/**
 * Data class representing the response containing player certificates.
 *
 * @property keyPair The key pair associated with the player.
 * @property expiresAt The expiration date and time of the certificates, in ISO 8601 format.
 * @property publicKeySignature An optional signature of the public key, if available.
 */

data class PlayerCertificatesResponse(
    val keyPair: KeyPairOut,
    val expiresAt: String,
    val publicKeySignature: String? = null
)
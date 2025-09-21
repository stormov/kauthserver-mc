package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices.*
import my.stormov.kauthserver.yggdrasil.api.dto.response.*
import my.stormov.kauthserver.yggdrasil.config.*
import org.springframework.stereotype.Service
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ProfileKeyService(private val props: YggProperties) {

    private val dtf = DateTimeFormatter.ISO_INSTANT

    fun issueFor(userId: UUID, now: Instant): PlayerCertificatesResponse {
        val kpg = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
        val pair = kpg.generateKeyPair()
        val pubPem = toPem(pair.public, "PUBLIC KEY")
        val privPem = toPem(pair.private, "PRIVATE KEY")

        val expiresAt = now.plus(props.services.profileKeys.ttl)

        val signature = props.services.profileKeys.signingPrivateKey?.takeIf { it.isNotBlank() }?.let { pem ->
            val priv = loadPrivateFromPem(pem)
            sign("SHA256withRSA", priv, pair.public.encoded)
        }

        return PlayerCertificatesResponse(
            keyPair = KeyPairOut(publicKey = pubPem, privateKey = privPem),
            expiresAt = dtf.format(expiresAt),
            publicKeySignature = signature
        )
    }

    private fun toPem(key: Key, type: String): String {
        val b64 = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(key.encoded)
        return "-----BEGIN $type-----\n$b64\n-----END $type-----"
    }

    private fun loadPrivateFromPem(pem: String): PrivateKey {
        val content = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(content)
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }

    private fun sign(alg: String, privateKey: PrivateKey, data: ByteArray): String {
        val sig = Signature.getInstance(alg)
        sig.initSign(privateKey)
        sig.update(data)
        return Base64.getEncoder().encodeToString(sig.sign())
    }
}
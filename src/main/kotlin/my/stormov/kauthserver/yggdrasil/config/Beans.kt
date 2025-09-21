package my.stormov.kauthserver.yggdrasil.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.*

@Configuration
class Beans(private val props: AuthProperties) {

    @Bean
    fun passwordEncoder(rng: SecureRandom): PasswordEncoder =
        BCryptPasswordEncoder(props.bcryptStrength, rng)

    @Bean
    fun secureRandom(): SecureRandom = SecureRandom()

    @Bean
    fun cacheManager(): CacheManager {
        val cm = CaffeineCacheManager("tokens")
        cm.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofMinutes(30))
        )
        return cm
    }

    @Bean
    fun tokenGenerator(rng: SecureRandom) = TokenGenerator(rng)

    @Bean
    fun tokenHasher() = TokenHasher(props.pepper)
}

class TokenGenerator(private val rng: SecureRandom) {
    fun accessToken(): String {
        val bytes = ByteArray(32)
        rng.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    fun clientToken(): String = UUID.randomUUID().toString().replace("-", "")
}

class TokenHasher(private val pepper: String) {
    fun hash(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((token + pepper).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
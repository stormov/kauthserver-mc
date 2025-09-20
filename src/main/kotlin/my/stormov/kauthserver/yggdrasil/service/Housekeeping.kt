package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.repository.ServerJoinRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class Housekeeping(private val joins: ServerJoinRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "PT5M")
    fun purgeExpiredJoins() {
        val n = joins.deleteAllByExpiresAtBefore(Instant.now())
        if (n > 0) log.info("Purged $n expired server join records")
    }
}
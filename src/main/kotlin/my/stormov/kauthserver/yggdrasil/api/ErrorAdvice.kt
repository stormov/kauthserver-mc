package my.stormov.kauthserver.yggdrasil.api

import my.stormov.kauthserver.yggdrasil.api.dto.YggError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class ErrorAdvice {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleRse(ex: ResponseStatusException): ResponseEntity<YggError> {
        val status = ex.statusCode
        val ygg = YggError(
            error = if (status == HttpStatus.FORBIDDEN) "ForbiddenOperationException" else "IllegalArgumentException",
            errorMessage = ex.reason ?: "Operation failed"
        )
        return ResponseEntity.status(status).body(ygg)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<YggError> {
        val ygg = YggError(error = "InternalServerError", errorMessage = "An error has occurred.")
        return ResponseEntity.status(500).body(ygg)
    }
}
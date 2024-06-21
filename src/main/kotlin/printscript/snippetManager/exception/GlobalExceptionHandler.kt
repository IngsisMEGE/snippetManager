package printscript.snippetManager.exception

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage!! }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<String> {
        // Puedes personalizar este mensaje de error
        val errorMessage = "Error al procesar la solicitud: Asegúrate de incluir todos los campos requeridos."
        return ResponseEntity.badRequest().body(errorMessage)
    }
}

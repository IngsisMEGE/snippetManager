package printscript.snippetManager.controller.payload.request

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank

data class SnippetEditDTO(
    @NotBlank val code: String,
) {
    @AssertTrue(message = "El código no puede estar vacío")
    fun isCodeNotEmpty(): Boolean = code.trim().isNotEmpty()
}

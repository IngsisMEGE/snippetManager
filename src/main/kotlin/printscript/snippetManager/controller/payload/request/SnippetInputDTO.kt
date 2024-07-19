package printscript.snippetManager.controller.payload.request

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank

data class SnippetInputDTO(
    @NotBlank val name: String,
    @NotBlank val language: String,
    @NotBlank val code: String,
    @NotBlank val extension: String,
) {
    @AssertTrue(message = "El nombre no puede estar vacío")
    fun isNameNotEmpty(): Boolean = name.trim().isNotEmpty()

    @AssertTrue(message = "El lenguaje no puede estar vacío")
    fun isLanguageNotEmpty(): Boolean = language.trim().isNotEmpty()

    @AssertTrue(message = "El código no puede estar vacío")
    fun isCodeNotEmpty(): Boolean = code.trim().isNotEmpty()

    @AssertTrue(message = "La extensión no puede estar vacía")
    fun isExtensionNotEmpty(): Boolean = extension.trim().isNotEmpty()
}

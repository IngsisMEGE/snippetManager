package printscript.snippetManager.controller.payload.request

import jakarta.validation.constraints.NotBlank

data class SnippetInputDTO(
    @NotBlank val name: String,
    @NotBlank val language: String,
    @NotBlank val code: String,
)

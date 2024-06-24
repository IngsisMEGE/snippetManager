package printscript.snippetManager.controller.payload.response

data class SnippetOutputDTO(
    val id: Long,
    val name: String,
    val language: String,
    val code: String,
    val author: String,
)

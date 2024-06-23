package printscript.snippetManager.controller.payload.response

data class SnippetViewDTO(
    val id: Long,
    val name: String,
    val language: String,
    val author: String,
    val status: String,
)

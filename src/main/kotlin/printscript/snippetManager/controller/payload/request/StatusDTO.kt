package printscript.snippetManager.controller.payload.request

import printscript.snippetManager.enums.SnippetStatusEnum

data class StatusDTO(
    val status: SnippetStatusEnum,
    val id: Long,
    val ownerEmail: String,
)

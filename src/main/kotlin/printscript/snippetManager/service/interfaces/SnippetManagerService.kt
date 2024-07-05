package printscript.snippetManager.service.interfaces

import org.springframework.data.domain.Page
import org.springframework.security.oauth2.jwt.Jwt
import printscript.snippetManager.controller.payload.request.FilterDTO
import printscript.snippetManager.controller.payload.request.SnippetEditDTO
import printscript.snippetManager.controller.payload.request.SnippetInputDTO
import printscript.snippetManager.controller.payload.request.UpdateAction
import printscript.snippetManager.controller.payload.response.SnippetOutputDTO
import printscript.snippetManager.controller.payload.response.SnippetViewDTO
import printscript.snippetManager.enums.SnippetStatusEnum
import reactor.core.publisher.Mono

interface SnippetManagerService {
    fun createSnippet(
        snippet: SnippetInputDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO>

    fun editSnippet(
        id: Long,
        editedCode: SnippetEditDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO>

    fun searchSnippetsByFilter(
        filter: FilterDTO,
        page: Int,
        size: Int,
        userData: Jwt,
    ): Page<SnippetViewDTO>

    fun getSnippetById(
        id: Long,
        userData: Jwt,
    ): SnippetOutputDTO

    fun shareSnippet(
        id: Long,
        userData: Jwt,
        shareEmail: String,
    )

    fun updateSnippetStatus(
        id: Long,
        status: SnippetStatusEnum,
        authorEmail: String,
    )

    fun updateAllStatus(authorEmail: String, action : UpdateAction)

    fun updateSnippetSCA()

    fun updateSnippetFormat()
}

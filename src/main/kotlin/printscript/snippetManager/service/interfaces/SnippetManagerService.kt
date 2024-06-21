package printscript.snippetManager.service.interfaces

import org.springframework.security.oauth2.core.oidc.user.OidcUser
import printscript.snippetManager.controller.payload.request.SnippetInputDTO
import printscript.snippetManager.controller.payload.response.SnippetOutputDTO
import reactor.core.publisher.Mono

interface SnippetManagerService {
    fun createSnippet(
        snippet: SnippetInputDTO,
        userData: OidcUser,
    ): Mono<SnippetOutputDTO>
}

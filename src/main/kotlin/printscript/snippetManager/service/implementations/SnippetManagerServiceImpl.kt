package printscript.snippetManager.service.implementations

import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.SnippetInputDTO
import printscript.snippetManager.controller.payload.response.SnippetOutputDTO
import printscript.snippetManager.entity.Snippet
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.repository.SnippetStatusRepository
import printscript.snippetManager.service.interfaces.AssetService
import printscript.snippetManager.service.interfaces.SnippetManagerService
import reactor.core.publisher.Mono

@Service
class SnippetManagerServiceImpl(
    val snippetRepository: SnippetRepository,
    val snippetStatusRepository: SnippetStatusRepository,
    val assetService: AssetService,
) :
    SnippetManagerService {
    override fun createSnippet(
        snippet: SnippetInputDTO,
        userData: OidcUser,
    ): Mono<SnippetOutputDTO> {
        val savedSnippet =
            snippetRepository.save(
                Snippet(
                    name = snippet.name,
                    language = snippet.language,
                    author = userData.email,
                ),
            )

        val snippetStatus =
            snippetStatusRepository.save(
                SnippetStatus(
                    userEmail = userData.email,
                    snippet = savedSnippet,
                    status = printscript.snippetManager.enums.SnippetStatus.PENDING,
                ),
            )

        return assetService.saveSnippetInBucket(savedSnippet.id, snippet.code)
            .then(
                Mono.just(
                    SnippetOutputDTO(
                        id = savedSnippet.id,
                        name = savedSnippet.name,
                        language = savedSnippet.language,
                        code = snippet.code,
                        author = savedSnippet.author,
                    ),
                ),
            ).onErrorResume { error ->
                snippetStatusRepository.delete(snippetStatus)
                snippetRepository.delete(savedSnippet)
                throw Error("Error al guardar el snippet: ${error.message}")
            }
    }
}

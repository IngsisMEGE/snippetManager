package printscript.snippetManager.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import printscript.snippetManager.controller.payload.request.SnippetInputDTO
import printscript.snippetManager.controller.payload.response.SnippetOutputDTO
import printscript.snippetManager.service.interfaces.SnippetManagerService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/snippetManager")
class SnippetManagerController(val snippetManagerService: SnippetManagerService) {
    @PostMapping("/create")
    fun createSnippetInEditor(
        @Valid @RequestBody snippet: SnippetInputDTO,
        @AuthenticationPrincipal userData: OidcUser,
    ): Mono<ResponseEntity<SnippetOutputDTO>> {
        return try {
            snippetManagerService.createSnippet(snippet, userData)
                .map { ResponseEntity.ok(it) }
        } catch (e: Exception) {
            Mono.just(ResponseEntity.badRequest().build())
        }
    }
}

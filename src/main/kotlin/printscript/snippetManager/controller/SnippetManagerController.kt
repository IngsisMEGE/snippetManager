package printscript.snippetManager.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import printscript.snippetManager.controller.payload.request.SnippetEditDTO
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
        @AuthenticationPrincipal userData: Jwt,
    ): Mono<ResponseEntity<SnippetOutputDTO>> {
        return try {
            snippetManagerService.createSnippet(snippet, userData)
                .map { ResponseEntity.ok(it) }
        } catch (e: Exception) {
            Mono.just(ResponseEntity.badRequest().build())
        }
    }

    @PostMapping("/edit/{id}")
    fun editSnippetInEditor(
        @PathVariable id: Long,
        @Valid @RequestBody snippet: SnippetEditDTO,
        @AuthenticationPrincipal userData: Jwt,
    ): Mono<ResponseEntity<SnippetOutputDTO>> {
        return try {
            snippetManagerService.editSnippet(id, snippet, userData)
                .map { ResponseEntity.ok(it) }
        } catch (e: Exception) {
            println(e.message)
            Mono.just(ResponseEntity.badRequest().build())
        }
    }
}

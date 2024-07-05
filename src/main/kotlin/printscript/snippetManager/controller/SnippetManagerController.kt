package printscript.snippetManager.controller

import jakarta.validation.Valid
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import printscript.snippetManager.controller.payload.request.*
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
            snippetManagerService.createSnippet(snippet, userData).map { ResponseEntity.ok(it) }
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
            snippetManagerService.editSnippet(id, snippet, userData).map { ResponseEntity.ok(it) }
        } catch (e: Exception) {
            Mono.just(ResponseEntity.badRequest().build())
        }
    }

    @PostMapping("/search")
    fun searchSnippets(
        @RequestBody filter: FilterDTO,
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal userData: Jwt,
    ): ResponseEntity<*> {
        return ResponseEntity.ok(snippetManagerService.searchSnippetsByFilter(filter, page, size, userData))
    }

    @GetMapping("/get/{id}")
    fun getSnippet(
        @PathVariable id: Long,
        @AuthenticationPrincipal userData: Jwt,
    ): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(snippetManagerService.getSnippetById(id, userData))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PostMapping("/share/{id}")
    fun shareSnippet(
        @PathVariable id: Long,
        @AuthenticationPrincipal userData: Jwt,
        @RequestParam shareEmail: String,
    ): ResponseEntity<String> {
        return try {
            snippetManagerService.shareSnippet(id, userData, shareEmail)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("/pending/user/sca")
    fun updateSnippetFromUpdateRulesSCA(
        @AuthenticationPrincipal userData: Jwt,
        @RequestBody action : UpdateAction
    ): ResponseEntity<Any> {
        return try {
            snippetManagerService.updateAllStatus(userData.claims["email"].toString(), action)
            ResponseEntity.ok("Status Updated Correctly")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("/pending/user/format")
    fun updateSnippetFromUpdateRulesFormatter(
        @AuthenticationPrincipal userData: Jwt
    ) : ResponseEntity<Any> {

    }
}

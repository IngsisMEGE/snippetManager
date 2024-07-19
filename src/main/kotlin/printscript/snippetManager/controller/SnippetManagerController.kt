package printscript.snippetManager.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import printscript.snippetManager.controller.payload.request.*
import printscript.snippetManager.controller.payload.response.*
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
        @RequestBody rules: SCARuleEndpoint,
    ): ResponseEntity<Any> {
        return try {
            val language = Language.Printscript
            snippetManagerService.updateSnippetSCA(SCARulesDTO(rules.scaRules, language), userData)
            ResponseEntity.ok("Status Updated Correctly")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @PutMapping("/pending/user/format")
    fun updateSnippetFromUpdateRulesFormatter(
        @AuthenticationPrincipal userData: Jwt,
        @RequestBody rules: FormatRulesDTO,
    ): ResponseEntity<Any> {
        return try {
            snippetManagerService.updateSnippetFormat(rules, userData)
            ResponseEntity.ok("Status Updated Correctly")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteSnippet(
        @PathVariable id: Long,
        @AuthenticationPrincipal userData: Jwt,
    ): ResponseEntity<String> {
        return try {
            snippetManagerService.deleteSnippet(id, userData)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    @GetMapping("/fileTypes")
    fun getFileTypes(): ResponseEntity<List<FileTypeDTO>> {
        return ResponseEntity.ok(snippetManagerService.getFileTypes())
    }

    @PutMapping("/update/status")
    fun updateSnippetStatus(
        @AuthenticationPrincipal userData: Jwt,
        @RequestBody statusDTO: StatusDTO,
    ): ResponseEntity<Any> {
        return try {
            snippetManagerService.updateSnippetStatus(statusDTO.id, statusDTO.status, statusDTO.ownerEmail)
            ResponseEntity.ok("Status Updated Correctly")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}

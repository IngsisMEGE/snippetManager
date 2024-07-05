package printscript.snippetManager.service.interfaces

import org.springframework.security.oauth2.jwt.Jwt
import printscript.snippetManager.controller.payload.request.FormatRulesDTO
import printscript.snippetManager.controller.payload.request.SCARulesDTO

interface PrintScriptService {

    fun analyzeAllSnippets(scaSnippetRules : SCARulesDTO, userData : Jwt)

    fun formatAllSnippets(formatSnippetRules : FormatRulesDTO, userData : Jwt)
}
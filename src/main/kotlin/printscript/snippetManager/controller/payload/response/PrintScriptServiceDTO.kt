package printscript.snippetManager.controller.payload.response

import org.springframework.security.oauth2.jwt.Jwt

data class RulesDTO(
    val name: String,
    var value: String,
)

data class FormatSnippetWithRulesDTO(
    val snippetId: Long,
    val formatRules: List<RulesDTO>,
    val lintingRules: List<RulesDTO>,
)

data class SCASnippetWithRulesDTO(
    val snippetId: Long,
    val scaRules: List<RulesDTO>,
    val lintingRules: List<RulesDTO>,
)

data class SCASnippetWithRulesRedisDTO(
    val scaSnippet: SCASnippetWithRulesDTO,
    val userData: Jwt,
)

data class FormatSnippetWithRulesRedisDTO(
    val formatSnippetWithRules: FormatSnippetWithRulesDTO,
    val userData: Jwt,
)

package printscript.snippetManager.controller.payload.request

import printscript.snippetManager.controller.payload.response.*

data class FormatRulesDTO(
    val formatRules: List<RulesDTO>,
    val lintingRules: List<RulesDTO>,
)

data class SCARulesDTO(
    val scaRules: List<RulesDTO>,
    val language: Language,
)

data class SCARuleEndpoint(
    val scaRules: List<RulesDTO>,
    val language: String,
)

data class UpdateAction(
    val lexer: Boolean,
    val formatter: Boolean,
    val sca: Boolean,
)

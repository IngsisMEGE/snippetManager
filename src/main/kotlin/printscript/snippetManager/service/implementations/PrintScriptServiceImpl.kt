package printscript.snippetManager.service.implementations

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.FormatRulesDTO
import printscript.snippetManager.controller.payload.request.SCARulesDTO
import printscript.snippetManager.controller.payload.response.FormatSnippetWithRulesDTO
import printscript.snippetManager.controller.payload.response.FormatSnippetWithRulesRedisDTO
import printscript.snippetManager.controller.payload.response.SCASnippetWithRulesDTO
import printscript.snippetManager.controller.payload.response.SCASnippetWithRulesRedisDTO
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.service.interfaces.PrintScriptService

@Service
class PrintScriptServiceImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val snippetRepository: SnippetRepository,
) : PrintScriptService {
    private val objectMapper = jacksonObjectMapper()

    override fun analyzeAllSnippets(
        scaSnippetRules: SCARulesDTO,
        userData: Jwt,
    ) {
        val userEmail = userData.claims["email"].toString()
        val snippets = snippetRepository.getSnippetsByAuthor(userEmail)

        for (snippetData in snippets) {
            val scaRulesRedisDTO =
                SCASnippetWithRulesRedisDTO(
                    SCASnippetWithRulesDTO(
                        snippetData.id,
                        scaSnippetRules.scaRules,
                        scaSnippetRules.lintingRules,
                    ),
                    userData,
                )
            val requestData = objectMapper.writeValueAsString(scaRulesRedisDTO)
            redisTemplate.opsForList().rightPush("snippet_sca_queue", requestData)
        }
    }

    override fun formatAllSnippets(
        formatSnippetRules: FormatRulesDTO,
        userData: Jwt,
    ) {
        val userEmail = userData.claims["email"].toString()
        val snippets = snippetRepository.getSnippetsByAuthor(userEmail)

        for (snippetData in snippets) {
            val formatRulesRedisDTO =
                FormatSnippetWithRulesRedisDTO(
                    FormatSnippetWithRulesDTO(
                        snippetData.id,
                        formatSnippetRules.formatRules,
                        formatSnippetRules.lintingRules,
                    ),
                    userData,
                )
            val requestData = objectMapper.writeValueAsString(formatRulesRedisDTO)
            redisTemplate.opsForList().rightPush("snippet_formatting_queue", requestData)
        }
    }
}

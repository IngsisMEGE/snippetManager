package printscript.snippetManager.service.implementations

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.FormatRulesDTO
import printscript.snippetManager.controller.payload.request.SCARulesDTO
import printscript.snippetManager.controller.payload.response.*
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.service.interfaces.PrintScriptService

@Service
class PrintScriptServiceImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val snippetRepository: SnippetRepository,
    private val objectMapper: ObjectMapper,
) : PrintScriptService {
    private val logger: Logger = LoggerFactory.getLogger(PrintScriptServiceImpl::class.java)

    override fun analyzeAllSnippets(
        scaSnippetRules: SCARulesDTO,
        userData: Jwt,
    ) {
        logger.debug("Entering analyzeAllSnippets")
        val userEmail = userData.claims["email"].toString()
        val snippets = snippetRepository.getSnippetsByAuthor(userEmail)
        logger.info("Fetched ${snippets.size} snippets for analysis for user: $userEmail")

        for (snippetData in snippets) {
            val scaRulesRedisDTO =
                SCASnippetWithRulesRedisDTO(
                    SCASnippetWithRulesDTO(
                        snippetData.id,
                        scaSnippetRules.scaRules,
                        scaSnippetRules.language,
                    ),
                    userData,
                )
            val requestData = objectMapper.writeValueAsString(scaRulesRedisDTO)
            redisTemplate.opsForList().rightPush("snippet_sca_queue", requestData)
            logger.info("Queued snippet with id: ${snippetData.id} for SCA analysis")
        }
        logger.debug("Exiting analyzeAllSnippets")
    }

    override fun formatAllSnippets(
        formatSnippetRules: FormatRulesDTO,
        userData: Jwt,
    ) {
        logger.debug("Entering formatAllSnippets")
        val userEmail = userData.claims["email"].toString()
        val snippets = snippetRepository.getSnippetsByAuthor(userEmail)
        logger.info("Fetched ${snippets.size} snippets for formatting for user: $userEmail")

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
            logger.info("Queued snippet with id: ${snippetData.id} for formatting")
        }
        logger.debug("Exiting formatAllSnippets")
    }

    override fun analyzeSnippet(
        snippetId: Long,
        userData: Jwt,
    ) {
        logger.debug("Entering analyzeSnippet with snippetId: $snippetId")
        val scaRulesRedisDTO =
            SCASnippetRedisDTO(
                snippetId,
                userData,
                Language.Printscript,
            )
        val requestData = objectMapper.writeValueAsString(scaRulesRedisDTO)
        redisTemplate.opsForList().rightPush("snippet_sca_unique_queue", requestData)
        logger.info("Queued snippet with id: $snippetId for SCA analysis")
        logger.debug("Exiting analyzeSnippet with snippetId: $snippetId")
    }
}

package printscript.snippetManager.service.implementations

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.StatusDTO
import printscript.snippetManager.service.interfaces.SnippetManagerService

@Service
class SnippetProcessingServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val snippetManagerService: SnippetManagerService,
    private val objectMapper: ObjectMapper,
) {
    private val logger: Logger = LoggerFactory.getLogger(SnippetProcessingServiceImpl::class.java)

    @Scheduled(fixedDelay = 1000)
    fun processQueue() {
        val requestData = redisTemplate.opsForList().leftPop("snippet_sca_status")
        if (requestData != null) {
            logger.debug("Processing snippet status update request")
            val updateStatus: StatusDTO = objectMapper.readValue(requestData.toString(), StatusDTO::class.java)
            snippetManagerService.updateSnippetStatus(
                updateStatus.id,
                updateStatus.status,
                updateStatus.ownerEmail,
            )
        }
    }
}

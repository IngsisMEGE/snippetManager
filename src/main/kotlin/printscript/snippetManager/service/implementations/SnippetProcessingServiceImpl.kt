package printscript.snippetManager.service.implementations

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.StatusDTO
import printscript.snippetManager.service.interfaces.SnippetManagerService

@Service
@EnableScheduling
class SnippetProcessingServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val snippetManagerService: SnippetManagerService,
    private val objectMapper: ObjectMapper,
) {
    private val logger: Logger = LoggerFactory.getLogger(SnippetProcessingServiceImpl::class.java)

    @Scheduled(fixedDelay = 10000)
    fun processQueue() {
        println("called function")
        val requestData = redisTemplate.opsForList().rightPop("snippet_sca_queue")
        if (requestData != null) {
            logger.info("Processing snippet status update request")
            println("Processing snippet sca queue")
            val updateStatus: StatusDTO = objectMapper.readValue(requestData.toString(), StatusDTO::class.java)
            snippetManagerService.updateSnippetStatus(
                updateStatus.id,
                updateStatus.status,
                updateStatus.ownerEmail,
            )
        }
    }
}

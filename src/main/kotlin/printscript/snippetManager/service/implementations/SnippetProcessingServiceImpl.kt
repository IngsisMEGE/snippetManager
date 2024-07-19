package printscript.snippetManager.service.implementations

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.StatusDTO
import printscript.snippetManager.service.interfaces.SnippetManagerService

@Service
class SnippetProcessingServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val snippetManagerService: SnippetManagerService,
) {
    private val objectMapper = jacksonObjectMapper()

    @Scheduled(fixedDelay = 1000)
    fun processQueue() {
        val requestData = redisTemplate.opsForList().leftPop("snippet_sca_status")
        if (requestData != null) {
            val updateStatus: StatusDTO = objectMapper.readValue(requestData.toString(), StatusDTO::class.java)
            snippetManagerService.updateSnippetStatus(
                updateStatus.id,
                updateStatus.status,
                updateStatus.ownerEmail,
            )
        }
    }
}

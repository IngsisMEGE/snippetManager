package printscript.snippetManager.service.implementations

import io.github.cdimascio.dotenv.Dotenv
import logs.CorrIdFilter.Companion.CORRELATION_ID_KEY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import printscript.snippetManager.service.interfaces.AssetService
import reactor.core.publisher.Mono

@Service
class AssetServiceImpl(
    private val webClient: WebClient,
    @Autowired private val dotenv: Dotenv,
) : AssetService {
    private val logger: Logger = LoggerFactory.getLogger(AssetServiceImpl::class.java)
    private val bucketUrl = "${dotenv["BUCKET_URL"]}/v1/asset/snippet"

    override fun saveSnippetInBucket(
        snippetId: Long,
        code: String,
    ): Mono<Void> {
        logger.debug("Entering saveSnippetInBucket with snippetId: $snippetId")
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return webClient.post()
            .uri(snippetUrl)
            .bodyValue(code)
            .headers { httpHeaders -> httpHeaders.addAll(headers) }
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                logger.error("Error status received while saving snippet with id: $snippetId")
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error saving snippet: $errorBody") }
            })
            .toBodilessEntity()
            .then()
            .doOnSuccess { logger.info("Successfully saved snippet with id: $snippetId") }
            .doOnError { e -> logger.error("Error saving snippet with id: $snippetId", e) }
            .doFinally { logger.debug("Exiting saveSnippetInBucket with snippetId: $snippetId") }
    }

    override fun getSnippetFromBucket(snippetId: Long): String {
        logger.debug("Entering getSnippetFromBucket with snippetId: $snippetId")
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return try {
            val snippet =
                webClient.get()
                    .uri(snippetUrl)
                    .headers { httpHeaders -> httpHeaders.addAll(headers) }
                    .retrieve()
                    .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                        logger.error("Error status received while getting snippet with id: $snippetId")
                        it.bodyToMono(String::class.java)
                            .map { errorBody -> Exception("Error getting snippet: $errorBody") }
                    })
                    .bodyToMono(String::class.java)
                    .block() ?: throw Exception("Error getting snippet")
            logger.info("Successfully retrieved snippet with id: $snippetId")
            snippet
        } catch (e: Exception) {
            logger.error("Error retrieving snippet with id: $snippetId", e)
            throw e
        } finally {
            logger.debug("Exiting getSnippetFromBucket with snippetId: $snippetId")
        }
    }

    override fun deleteSnippetFromBucket(snippetId: Long): Mono<Void> {
        logger.debug("Entering deleteSnippetFromBucket with snippetId: $snippetId")
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return webClient.delete()
            .uri(snippetUrl)
            .headers { httpHeaders -> httpHeaders.addAll(headers) }
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                logger.error("Error status received while deleting snippet with id: $snippetId")
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error deleting snippet: $errorBody") }
            })
            .toBodilessEntity()
            .then()
            .doOnSuccess { logger.info("Successfully deleted snippet with id: $snippetId") }
            .doOnError { e -> logger.error("Error deleting snippet with id: $snippetId", e) }
            .doFinally { logger.debug("Exiting deleteSnippetFromBucket with snippetId: $snippetId") }
    }

    private fun getHeader(): HttpHeaders {
        val correlationId = MDC.get(CORRELATION_ID_KEY)
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("X-Correlation-Id", correlationId)
        }
    }
}

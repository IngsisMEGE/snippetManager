package printscript.snippetManager.service.implementations

import io.github.cdimascio.dotenv.Dotenv
import log.CorrIdFilter.Companion.CORRELATION_ID_KEY
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
    private val bucketUrl = "${dotenv["BUCKET_URL"]}/v1/asset/snippet"

    override fun saveSnippetInBucket(
        snippetId: Long,
        code: String,
    ): Mono<Void> {
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return webClient.post()
            .uri(snippetUrl)
            .bodyValue(code)
            .headers { httpHeaders -> httpHeaders.addAll(headers) }
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error al guardar el snippet: $errorBody") }
            })
            .toBodilessEntity()
            .then()
    }

    override fun getSnippetFromBucket(snippetId: Long): String {
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return webClient.get()
            .uri(snippetUrl)
            .headers { httpHeaders -> httpHeaders.addAll(headers) }
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error al obtener el snippet: $errorBody") }
            })
            .bodyToMono(String::class.java)
            .block() ?: throw Exception("Error al obtener el snippet")
    }

    override fun deleteSnippetFromBucket(snippetId: Long): Mono<Void> {
        val snippetUrl = "$bucketUrl/$snippetId"
        val headers = getHeader()
        return webClient.delete()
            .uri(snippetUrl)
            .headers { httpHeaders -> httpHeaders.addAll(headers) }
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error al eliminar el snippet: $errorBody") }
            })
            .toBodilessEntity()
            .then()
    }

    private fun getHeader(): HttpHeaders {
        val correlationId = MDC.get(CORRELATION_ID_KEY)
        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Correlation-Id", correlationId)
            }
        return headers
    }
}

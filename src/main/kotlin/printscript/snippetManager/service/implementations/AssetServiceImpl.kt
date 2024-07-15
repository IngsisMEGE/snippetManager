package printscript.snippetManager.service.implementations

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.beans.factory.annotation.Autowired
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

        return webClient.post()
            .uri(snippetUrl)
            .bodyValue(code)
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

        return webClient.get()
            .uri(snippetUrl)
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

        return webClient.delete()
            .uri(snippetUrl)
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }, {
                it.bodyToMono(String::class.java)
                    .map { errorBody -> Exception("Error al eliminar el snippet: $errorBody") }
            })
            .toBodilessEntity()
            .then()
    }
}

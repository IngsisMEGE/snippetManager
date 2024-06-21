package printscript.snippetManager.service.implementations

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import printscript.snippetManager.service.interfaces.AssetService
import reactor.core.publisher.Mono

@Service
class AssetServiceImpl(
    val webClient: WebClient,
) : AssetService {
    private val bucketUrl = "http://localhost:8080/v1/asset/snippet" // deberia ser una variable de entorno

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
        TODO("Not yet implemented")
    }

    override fun deleteSnippetFromBucket(snippetId: Long) {
        TODO("Not yet implemented")
    }
}

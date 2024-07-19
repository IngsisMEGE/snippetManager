package printscript.snippetManager.service.interfaces

import reactor.core.publisher.Mono

interface AssetService {
    fun saveSnippetInBucket(
        snippetId: Long,
        code: String,
    ): Mono<Void>

    fun getSnippetFromBucket(snippetId: Long): String

    fun deleteSnippetFromBucket(snippetId: Long): Mono<Void>
}

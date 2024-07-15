package printscript.snippetManager.service.implementations

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.*
import printscript.snippetManager.controller.payload.response.SnippetOutputDTO
import printscript.snippetManager.controller.payload.response.SnippetViewDTO
import printscript.snippetManager.entity.SharedSnippet
import printscript.snippetManager.entity.Snippet
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.enums.SnippetStatusEnum
import printscript.snippetManager.repository.FilterRepository
import printscript.snippetManager.repository.SharedSnippetRepository
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.repository.SnippetStatusRepository
import printscript.snippetManager.service.interfaces.AssetService
import printscript.snippetManager.service.interfaces.PrintScriptService
import printscript.snippetManager.service.interfaces.SnippetManagerService
import reactor.core.publisher.Mono
import java.util.Locale

@Service
class SnippetManagerServiceImpl(
    val snippetRepository: SnippetRepository,
    val snippetStatusRepository: SnippetStatusRepository,
    val assetService: AssetService,
    val filterRepository: FilterRepository,
    val sharedSnippetRepository: SharedSnippetRepository,
    val printScriptService: PrintScriptService,
) :
    SnippetManagerService {
    override fun createSnippet(
        snippet: SnippetInputDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO> {
        val savedSnippet =
            snippetRepository.save(
                Snippet(
                    name = snippet.name,
                    language = snippet.language,
                    author = userData.claims["email"].toString(),
                ),
            )

        val snippetStatusEnum =
            snippetStatusRepository.save(
                SnippetStatus(
                    userEmail = userData.claims["email"].toString(),
                    snippet = savedSnippet,
                    status = SnippetStatusEnum.PENDING,
                ),
            )

        return assetService.saveSnippetInBucket(savedSnippet.id, snippet.code)
            .then(
                Mono.just(
                    SnippetOutputDTO(
                        id = savedSnippet.id,
                        name = savedSnippet.name,
                        language = savedSnippet.language,
                        code = snippet.code,
                        author = savedSnippet.author,
                        status = snippetStatusEnum.status.toString(),
                        extension = snippet.extension,
                    ),
                ),
            ).onErrorResume { error ->
                snippetStatusRepository.delete(snippetStatusEnum)
                snippetRepository.delete(savedSnippet)
                throw Error("Error al guardar el snippet: ${error.message}")
            }
    }

    override fun editSnippet(
        id: Long,
        editedCode: SnippetEditDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO> {
        val snippet = snippetRepository.findById(id)
        if (snippet.isEmpty) throw Error("Snippet no encontrado")

        if (snippet.get().author != userData.claims["email"].toString()) throw Error("No tienes permisos para editar este snippet")

        val snippetStatus = snippetStatusRepository.findBySnippetIdAndUserEmail(id, userData.claims["email"].toString())
        if (snippetStatus.isEmpty) throw Error("No te han compartido este snippet")
        snippetStatus.get().status = SnippetStatusEnum.PENDING

        snippetStatusRepository.save(snippetStatus.get())

        assetService.deleteSnippetFromBucket(id).block()

        val extension = languageToExtension(snippet.get().language)

        return assetService.saveSnippetInBucket(id, editedCode.code)
            .then(
                Mono.just(
                    SnippetOutputDTO(
                        id = snippet.get().id,
                        name = snippet.get().name,
                        language = snippet.get().language,
                        code = editedCode.code,
                        author = snippet.get().author,
                        status = snippetStatus.get().status.toString(),
                        extension = extension,
                    ),
                ),
            ).onErrorResume { error ->
                throw Error("Error al guardar el snippet: ${error.message}")
            }
    }

    override fun searchSnippetsByFilter(
        filter: FilterDTO,
        page: Int,
        size: Int,
        userData: Jwt,
    ): Page<SnippetViewDTO> {
        val pageAndSizeRequest: Pageable = PageRequest.of(page, size, Sort.by("id").descending())
        return filterRepository.filterSnippets(filter, pageAndSizeRequest, userData.claims["email"].toString())
    }

    override fun getSnippetById(
        id: Long,
        userData: Jwt,
    ): SnippetOutputDTO {
        val snippet = snippetRepository.findById(id)
        if (snippet.isEmpty) throw Exception("Snippet no encontrado")

        val email = userData.claims["email"].toString()

        if (snippet.get().author != email || sharedSnippetRepository.findBySnippetIdAndUserEmail(id, email)) {
            throw Exception(
                "No tienes permisos para ver este snippet",
            )
        }

        val code = assetService.getSnippetFromBucket(id)
        val status = snippetStatusRepository.findBySnippetIdAndUserEmail(id, snippet.get().author)
        if (status.isEmpty) throw Exception("No se encontro el estado del snippet")
        val extension = languageToExtension(snippet.get().language)

        return SnippetOutputDTO(
            id = snippet.get().id,
            name = snippet.get().name,
            language = snippet.get().language,
            code = code,
            author = snippet.get().author,
            status = status.get().status.toString(),
            extension = extension,
        )
    }

    override fun shareSnippet(
        id: Long,
        userData: Jwt,
        shareEmail: String,
    ) {
        val snippet = snippetRepository.findById(id)
        if (snippet.isEmpty) throw Exception("Snippet no encontrado")

        if (snippet.get().author != userData.claims["email"].toString()) throw Exception("No tienes permisos para compartir este snippet")
        if (sharedSnippetRepository.findBySnippetIdAndUserEmail(
                id,
                shareEmail,
            )
        ) {
            throw Exception("Ya compartiste este snippet con este usuario")
        }
        if (snippet.get().author == shareEmail) throw Exception("No puedes compartir un snippet contigo mismo")

        sharedSnippetRepository.save(
            SharedSnippet(
                userEmail = shareEmail,
                snippet = snippet.get(),
            ),
        )

        snippetStatusRepository.save(
            SnippetStatus(
                userEmail = shareEmail,
                snippet = snippet.get(),
                status = SnippetStatusEnum.PENDING,
            ),
        )
    }

    override fun updateSnippetStatus(
        id: Long,
        status: SnippetStatusEnum,
        authorEmail: String,
    ) {
        val snippetStatus = snippetStatusRepository.findBySnippetIdAndUserEmail(id, authorEmail)
        if (snippetStatus.isEmpty) throw Exception("No existe el snippet")

        snippetStatus.get().status = status
        snippetStatusRepository.save(snippetStatus.get())
    }

    override fun updateSnippetSCA(
        rules: SCARulesDTO,
        userData: Jwt,
    ) {
        val authorEmail = userData.claims["email"].toString()
        snippetStatusRepository.updateStatusByUserEmail(authorEmail, SnippetStatusEnum.PENDING)
        printScriptService.analyzeAllSnippets(rules, userData)
    }

    override fun updateSnippetFormat(
        rules: FormatRulesDTO,
        userData: Jwt,
    ) {
        val authorEmail = userData.claims["email"].toString()
        snippetStatusRepository.updateStatusByUserEmail(authorEmail, SnippetStatusEnum.PENDING)
        printScriptService.formatAllSnippets(rules, userData)
    }

    override fun deleteSnippet(
        id: Long,
        userData: Jwt,
    ) {
        val snippet = snippetRepository.findById(id)
        val email = userData.claims["email"].toString()
        if (snippet.get().author != email) {
            if (sharedSnippetRepository.findBySnippetIdAndUserEmail(id, email)) {
                sharedSnippetRepository.deleteBySnippetIdAndUserEmail(id, email)
                snippetStatusRepository.deleteBySnippetIdAndUserEmail(id, email)
                return
            } else {
                throw Exception("No tienes permisos para borrar este snippet")
            }
        }
        assetService.deleteSnippetFromBucket(id).block()
        snippetRepository.deleteById(id)
    }

    override fun getFileTypes(): List<String> {
        return languages
    }

    private fun languageToExtension(language: String): String {
        return when (language.lowercase(Locale.getDefault())) {
            "java" -> "java"
            "python" -> "py"
            "golang" -> "go"
            "printscript" -> "prs"
            else -> "txt"
        }
    }

    private val languages = listOf("java", "python", "golang", "printscript")
}

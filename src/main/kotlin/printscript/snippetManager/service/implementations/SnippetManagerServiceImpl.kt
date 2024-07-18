package printscript.snippetManager.service.implementations

import logs.CorrIdFilter.Companion.CORRELATION_ID_KEY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import printscript.snippetManager.controller.payload.request.*
import printscript.snippetManager.controller.payload.response.FileTypeDTO
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
) : SnippetManagerService {
    private val logger: Logger = LoggerFactory.getLogger(SnippetManagerServiceImpl::class.java)

    override fun createSnippet(
        snippet: SnippetInputDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO> {
        logger.debug("Entering createSnippet")
        val userEmail = userData.claims["email"].toString()
        return try {
            val savedSnippet =
                snippetRepository.save(
                    Snippet(
                        name = snippet.name,
                        language = snippet.language,
                        author = userEmail,
                    ),
                )

            val snippetStatusEnum =
                snippetStatusRepository.save(
                    SnippetStatus(
                        userEmail = userEmail,
                        snippet = savedSnippet,
                        status = SnippetStatusEnum.PENDING,
                    ),
                )

            assetService.saveSnippetInBucket(savedSnippet.id, snippet.code)
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
                ).doOnSuccess {
                    logger.info("Snippet created successfully for user: $userEmail")
                }.doOnError { error ->
                    snippetStatusRepository.delete(snippetStatusEnum)
                    snippetRepository.delete(savedSnippet)
                    logger.error("Error creating snippet for user: $userEmail", error)
                }
        } catch (e: Exception) {
            logger.error("Exception in createSnippet", e)
            throw e
        }
    }

    override fun editSnippet(
        id: Long,
        editedCode: SnippetEditDTO,
        userData: Jwt,
    ): Mono<SnippetOutputDTO> {
        logger.debug("Entering editSnippet with snippetId: $id")
        val userEmail = userData.claims["email"].toString()
        return try {
            val snippet =
                snippetRepository.findById(id).orElseThrow {
                    logger.error("Snippet not found with id: $id")
                    Exception("Snippet not found")
                }

            if (snippet.author != userEmail) {
                logger.warn("Unauthorized attempt to edit snippet by user: $userEmail")
                throw Exception("Unauthorized")
            }

            val snippetStatus =
                snippetStatusRepository.findBySnippetIdAndUserEmail(id, userEmail)
                    .orElseThrow {
                        logger.error("Snippet status not found for id: $id and user: $userEmail")
                        Exception("Snippet status not found")
                    }

            snippetStatus.status = SnippetStatusEnum.PENDING
            snippetStatusRepository.save(snippetStatus)

            assetService.deleteSnippetFromBucket(id).block()

            val extension = languageToExtension(snippet.language)

            assetService.saveSnippetInBucket(id, editedCode.code)
                .then(
                    Mono.just(
                        SnippetOutputDTO(
                            id = snippet.id,
                            name = snippet.name,
                            language = snippet.language,
                            code = editedCode.code,
                            author = snippet.author,
                            status = snippetStatus.status.toString(),
                            extension = extension,
                        ),
                    ),
                ).doOnSuccess {
                    logger.info("Snippet edited successfully for id: $id")
                }.doOnError { error ->
                    logger.error("Error editing snippet with id: $id", error)
                }
        } catch (e: Exception) {
            logger.error("Exception in editSnippet", e)
            throw e
        }
    }

    override fun searchSnippetsByFilter(
        filter: FilterDTO,
        page: Int,
        size: Int,
        userData: Jwt,
    ): Page<SnippetViewDTO> {
        logger.debug("Entering searchSnippetsByFilter")
        val userEmail = userData.claims["email"].toString()
        val pageAndSizeRequest: Pageable = PageRequest.of(page, size, Sort.by("id").descending())
        return filterRepository.filterSnippets(filter, pageAndSizeRequest, userEmail)
    }

    override fun getSnippetById(
        id: Long,
        userData: Jwt,
    ): SnippetOutputDTO {
        logger.debug("Entering getSnippetById with snippetId: $id")
        val userEmail = userData.claims["email"].toString()
        return try {
            val snippet =
                snippetRepository.findById(id).orElseThrow {
                    logger.error("Snippet not found with id: $id")
                    Exception("Snippet not found")
                }

            if (snippet.author != userEmail && !sharedSnippetRepository.findBySnippetIdAndUserEmail(id, userEmail)) {
                logger.warn("Unauthorized attempt to view snippet by user: $userEmail")
                throw Exception("Unauthorized")
            }

            val code = assetService.getSnippetFromBucket(id)
            val status =
                snippetStatusRepository.findBySnippetIdAndUserEmail(id, snippet.author)
                    .orElseThrow {
                        logger.error("Snippet status not found for id: $id and user: $userEmail")
                        Exception("Snippet status not found")
                    }

            val extension = languageToExtension(snippet.language)
            SnippetOutputDTO(
                id = snippet.id,
                name = snippet.name,
                language = snippet.language,
                code = code,
                author = snippet.author,
                status = status.status.toString(),
                extension = extension,
            )
        } catch (e: Exception) {
            logger.error("Exception in getSnippetById", e)
            throw e
        }
    }

    override fun shareSnippet(
        id: Long,
        userData: Jwt,
        shareEmail: String,
    ) {
        logger.debug("Entering shareSnippet with snippetId: $id")
        val userEmail = userData.claims["email"].toString()
        try {
            val snippet =
                snippetRepository.findById(id).orElseThrow {
                    logger.error("Snippet not found with id: $id")
                    Exception("Snippet not found")
                }

            if (snippet.author != userEmail) {
                logger.warn("Unauthorized attempt to share snippet by user: $userEmail")
                throw Exception("Unauthorized")
            }

            if (sharedSnippetRepository.findBySnippetIdAndUserEmail(id, shareEmail)) {
                logger.warn("Snippet already shared with user: $shareEmail")
                throw Exception("Snippet already shared with this user")
            }

            if (snippet.author == shareEmail) {
                logger.warn("Attempt to share snippet with self by user: $userEmail")
                throw Exception("Cannot share snippet with self")
            }

            sharedSnippetRepository.save(
                SharedSnippet(
                    userEmail = shareEmail,
                    snippet = snippet,
                ),
            )
            logger.info("Snippet shared successfully with user: $shareEmail")
        } catch (e: Exception) {
            logger.error("Exception in shareSnippet", e)
            throw e
        }
    }

    override fun updateSnippetStatus(
        id: Long,
        status: SnippetStatusEnum,
        authorEmail: String,
    ) {
        logger.debug("Entering updateSnippetStatus with snippetId: $id and status: $status")
        try {
            val snippetStatus =
                snippetStatusRepository.findBySnippetIdAndUserEmail(id, authorEmail)
                    .orElseThrow {
                        logger.error("Snippet status not found for id: $id and user: $authorEmail")
                        Exception("Snippet status not found")
                    }

            snippetStatus.status = status
            snippetStatusRepository.save(snippetStatus)
            logger.info("Snippet status updated to $status for snippetId: $id")
        } catch (e: Exception) {
            logger.error("Exception in updateSnippetStatus", e)
            throw e
        }
    }

    override fun updateSnippetSCA(
        rules: SCARulesDTO,
        userData: Jwt,
    ) {
        logger.debug("Entering updateSnippetSCA")
        try {
            val userEmail = userData.claims["email"].toString()
            snippetStatusRepository.updateStatusByUserEmail(userEmail, SnippetStatusEnum.PENDING)
            printScriptService.analyzeAllSnippets(rules, userData)
            logger.info("SCA analysis initiated for user: $userEmail")
        } catch (e: Exception) {
            logger.error("Exception in updateSnippetSCA", e)
            throw e
        }
    }

    override fun updateSnippetFormat(
        rules: FormatRulesDTO,
        userData: Jwt,
    ) {
        logger.debug("Entering updateSnippetFormat")
        try {
            val userEmail = userData.claims["email"].toString()
            snippetStatusRepository.updateStatusByUserEmail(userEmail, SnippetStatusEnum.PENDING)
            printScriptService.formatAllSnippets(rules, userData)
            logger.info("Formatting initiated for user: $userEmail")
        } catch (e: Exception) {
            logger.error("Exception in updateSnippetFormat", e)
            throw e
        }
    }

    override fun deleteSnippet(
        id: Long,
        userData: Jwt,
    ) {
        logger.debug("Entering deleteSnippet with snippetId: $id")
        val userEmail = userData.claims["email"].toString()
        try {
            val snippet =
                snippetRepository.findById(id).orElseThrow {
                    logger.error("Snippet not found with id: $id")
                    Exception("Snippet not found")
                }

            if (snippet.author != userEmail) {
                if (sharedSnippetRepository.findBySnippetIdAndUserEmail(id, userEmail)) {
                    sharedSnippetRepository.deleteBySnippetIdAndUserEmail(id, userEmail)
                    snippetStatusRepository.deleteBySnippetIdAndUserEmail(id, userEmail)
                    logger.info("Shared snippet deleted for user: $userEmail")
                } else {
                    logger.warn("Unauthorized attempt to delete snippet by user: $userEmail")
                    throw Exception("Unauthorized")
                }
            } else {
                assetService.deleteSnippetFromBucket(id).block()
                snippetRepository.deleteById(id)
                logger.info("Snippet deleted with id: $id")
            }
        } catch (e: Exception) {
            logger.error("Exception in deleteSnippet", e)
            throw e
        }
    }

    override fun getFileTypes(): List<FileTypeDTO> {
        logger.debug("Entering getFileTypes")
        return languages.map { FileTypeDTO(it, languageToExtension(it)) }
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

    private fun getHeader(): HttpHeaders {
        val correlationId = MDC.get(CORRELATION_ID_KEY)
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("X-Correlation-Id", correlationId)
        }
    }
}

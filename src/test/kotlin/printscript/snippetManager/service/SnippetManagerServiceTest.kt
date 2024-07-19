package printscript.snippetManager.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.security.oauth2.jwt.Jwt
import printscript.snippetManager.controller.payload.request.*
import printscript.snippetManager.controller.payload.response.FileTypeDTO
import printscript.snippetManager.controller.payload.response.RulesDTO
import printscript.snippetManager.controller.payload.response.SnippetViewDTO
import printscript.snippetManager.entity.SharedSnippet
import printscript.snippetManager.entity.Snippet
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.enums.SnippetStatusEnum
import printscript.snippetManager.repository.FilterRepository
import printscript.snippetManager.repository.SharedSnippetRepository
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.repository.SnippetStatusRepository
import printscript.snippetManager.service.implementations.SnippetManagerServiceImpl
import printscript.snippetManager.service.interfaces.AssetService
import printscript.snippetManager.service.interfaces.PrintScriptService
import printscript.snippetManager.service.interfaces.SnippetManagerService
import reactor.core.publisher.Mono
import java.util.Optional

class SnippetManagerServiceTest {
    private val snippetRepository: SnippetRepository = mock()
    private val snippetStatusRepository: SnippetStatusRepository = mock()
    private val assetService: AssetService = mock()
    private val filterRepository: FilterRepository = mock()
    private val sharedSnippetRepository: SharedSnippetRepository = mock()
    private val printScriptService: PrintScriptService = mock()
    private val snippetManagerService: SnippetManagerService =
        SnippetManagerServiceImpl(
            snippetRepository,
            snippetStatusRepository,
            assetService,
            filterRepository,
            sharedSnippetRepository,
            printScriptService,
        )

    val testJwt = "test"
    val jwt =
        Jwt.withTokenValue(testJwt)
            .header("alg", "RS256")
            .claim("email", "test@test.com")
            .build()

    @Test
    fun test001_createSnippetSuccess() {
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.save(mockedSnippet)).thenReturn(mockedSnippet)
        whenever(
            snippetStatusRepository.save(
                SnippetStatus(
                    "test@test.com",
                    mockedSnippet,
                    SnippetStatusEnum.PENDING,
                ),
            ),
        ).thenReturn(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING))
        whenever(assetService.saveSnippetInBucket(0, "println(\"Hello World\")")).thenReturn(Mono.empty())

        val dto = SnippetInputDTO("test", "java", "println(\"Hello World\")", "java")
        val snippet = snippetManagerService.createSnippet(dto, jwt).block()

        assertEquals(dto.name, snippet?.name)
        assertEquals(dto.language, snippet?.language)
        assertEquals(dto.code, snippet?.code)
        assertEquals(dto.language, snippet?.language)
    }

    @Test
    fun test002_createSnippetFail() {
        val dto = SnippetInputDTO("test", "java", "println(\"Hello World\")", "java")
        val mockedSnippet = Snippet("test", "java", "test@test.com")

        whenever(snippetRepository.save(mockedSnippet)).thenThrow(RuntimeException("Failed to save snippet"))

        val exception =
            assertThrows<RuntimeException> {
                snippetManagerService.createSnippet(dto, jwt).block()
            }

        assertEquals("Failed to save snippet", exception.message)
    }

    @Test
    fun test003_createSnippetSaveInBucketFail() {
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.save(mockedSnippet)).thenReturn(mockedSnippet)
        whenever(
            snippetStatusRepository.save(
                SnippetStatus(
                    "test@test.com",
                    mockedSnippet,
                    SnippetStatusEnum.PENDING,
                ),
            ),
        ).thenReturn(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING))
        whenever(
            assetService.saveSnippetInBucket(
                0,
                "println(\"Hello World\")",
            ),
        ).thenReturn(Mono.error(RuntimeException("Failed to save snippet in bucket")))

        val dto = SnippetInputDTO("test", "java", "println(\"Hello World\")", "java")
        val snippet =
            assertThrows<RuntimeException> {
                snippetManagerService.createSnippet(dto, jwt).block()
            }

        assertEquals("Failed to save snippet in bucket", snippet.message)
    }

    @Test
    fun test004_editSnippetSuccess() {
        val snippetId = 0L
        val editedCode = SnippetEditDTO("println(\"Hello World\")")
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(
            snippetStatusRepository.save(
                SnippetStatus(
                    "test@test.com",
                    mockedSnippet,
                    SnippetStatusEnum.PENDING,
                ),
            ),
        ).thenReturn(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING))
        whenever(assetService.deleteSnippetFromBucket(snippetId)).thenReturn(Mono.empty())
        whenever(assetService.saveSnippetInBucket(snippetId, "println(\"Hello World\")")).thenReturn(Mono.empty())

        val snippet = snippetManagerService.editSnippet(snippetId, editedCode, jwt).block()

        assertEquals(editedCode.code, snippet?.code)
    }

    @Test
    fun test005_editSnippetNotFoundFail() {
        val snippetId = 0L
        val editedCode = SnippetEditDTO("println(\"Hello World\")")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.editSnippet(snippetId, editedCode, jwt).block()
            }

        assertEquals("Snippet not found", exception.message)
    }

    @Test
    fun test006_editSnippetUnauthorizedFail() {
        val snippetId = 0L
        val editedCode = SnippetEditDTO("println(\"Hello World\")")
        val mockedSnippet = Snippet("test", "java", "test2@test2.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))

        val exception =
            assertThrows<Exception> {
                snippetManagerService.editSnippet(snippetId, editedCode, jwt).block()
            }

        assertEquals("Unauthorized", exception.message)
    }

    @Test
    fun test007_editSnippetWithoutStatusFail() {
        val snippetId = 0L
        val editedCode = SnippetEditDTO("println(\"Hello World\")")
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.editSnippet(snippetId, editedCode, jwt).block()
            }

        assertEquals("Snippet status not found", exception.message)
    }

    @Test
    fun test008_editSnippetSaveInBucketFail() {
        val snippetId = 0L
        val editedCode = SnippetEditDTO("println(\"Hello World\")")
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(
            snippetStatusRepository.save(
                SnippetStatus(
                    "test@test.com",
                    mockedSnippet,
                    SnippetStatusEnum.PENDING,
                ),
            ),
        ).thenReturn(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING))
        whenever(assetService.deleteSnippetFromBucket(snippetId)).thenReturn(Mono.empty())
        whenever(assetService.saveSnippetInBucket(snippetId, "println(\"Hello World\")")).thenReturn(
            Mono.error(
                RuntimeException("Failed to save snippet in bucket"),
            ),
        )

        val snippet =
            assertThrows<RuntimeException> { snippetManagerService.editSnippet(snippetId, editedCode, jwt).block() }

        assertEquals("Failed to save snippet in bucket", snippet.message)
    }

    @Test
    fun test009_searchSnippetSuccess() {
        whenever(snippetManagerService.searchSnippetsByFilter(FilterDTO(), 0, 5, jwt)).thenReturn(Page.empty())
        val snippets = snippetManagerService.searchSnippetsByFilter(FilterDTO(), 0, 5, jwt)

        assertEquals(Page.empty<SnippetViewDTO>(), snippets)
    }

    @Test
    fun test010_getSnippetByIdSuccess() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(sharedSnippetRepository.findBySnippetIdAndUserEmail(snippetId, "test@test.com")).thenReturn(true)
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(assetService.getSnippetFromBucket(snippetId)).thenReturn("println(\"Hello World\")")

        val snippet = snippetManagerService.getSnippetById(snippetId, jwt)

        assertEquals("println(\"Hello World\")", snippet.code)
    }

    @Test
    fun test011_getSnippetByIdNotFoundFail() {
        val snippetId = 0L
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.getSnippetById(snippetId, jwt)
            }

        assertEquals("Snippet not found", exception.message)
    }

    @Test
    fun test012_getSnippetByIdUnauthorizedFail() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test2@test2.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))

        val exception =
            assertThrows<Exception> {
                snippetManagerService.getSnippetById(snippetId, jwt)
            }

        assertEquals("Unauthorized", exception.message)
    }

    @Test
    fun test013_getSnippetByIdWithoutStatusFail() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.getSnippetById(snippetId, jwt)
            }

        assertEquals("Snippet status not found", exception.message)
    }

    @Test
    fun test014_shareSnippetSuccess() {
        val snippetId = 0L
        val userEmail = "test2@test2.com"
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(sharedSnippetRepository.findBySnippetIdAndUserEmail(snippetId, userEmail)).thenReturn(false)
        whenever(sharedSnippetRepository.save(SharedSnippet(userEmail, mockedSnippet))).thenReturn(
            SharedSnippet(
                userEmail,
                mockedSnippet,
            ),
        )

        val sharedSnippet = snippetManagerService.shareSnippet(snippetId, jwt, userEmail)

        assertEquals(Unit, sharedSnippet)
    }

    @Test
    fun test015_shareSnippetNotFoundFail() {
        val snippetId = 0L
        val userEmail = "test2@test2.com"
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.shareSnippet(snippetId, jwt, userEmail)
            }

        assertEquals("Snippet not found", exception.message)
    }

    @Test
    fun test016_shareSnippetUnauthorizedFail() {
        val snippetId = 0L
        val userEmail = "test2@test2.com"
        val mockedSnippet = Snippet("test", "java", "test3@test3.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))

        val exception =
            assertThrows<Exception> {
                snippetManagerService.shareSnippet(snippetId, jwt, userEmail)
            }

        assertEquals("Unauthorized", exception.message)
    }

    @Test
    fun test017_shareSnippetAlreadySharedFail() {
        val snippetId = 0L
        val userEmail = "test2@test2.com"
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(sharedSnippetRepository.findBySnippetIdAndUserEmail(snippetId, userEmail)).thenReturn(true)

        val exception =
            assertThrows<Exception> {
                snippetManagerService.shareSnippet(snippetId, jwt, userEmail)
            }

        assertEquals("Snippet already shared with this user", exception.message)
    }

    @Test
    fun test018_shareSnippetToOwnerFail() {
        val snippetId = 0L
        val userEmail = "test@test.com"
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))

        val exception =
            assertThrows<Exception> {
                snippetManagerService.shareSnippet(snippetId, jwt, userEmail)
            }

        assertEquals("Cannot share snippet with self", exception.message)
    }

    @Test
    fun test019_updateSnippetStatusSuccess() {
        val snippetId = 0L
        val status = SnippetStatusEnum.COMPLIANT
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(snippetStatusRepository.save(SnippetStatus("test@test.com", mockedSnippet, status))).thenReturn(
            SnippetStatus("test@test.com", mockedSnippet, status),
        )

        val updatedSnippet = snippetManagerService.updateSnippetStatus(snippetId, status, "test@test.com")

        assertEquals(Unit, updatedSnippet)
    }

    @Test
    fun test020_updateSnippetStatusNotFoundFail() {
        val snippetId = 0L
        val status = SnippetStatusEnum.COMPLIANT
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.updateSnippetStatus(snippetId, status, "test@test.com")
            }

        assertEquals("Snippet status not found", exception.message)
    }

    @Test
    fun test021_updateSnippetSCASuccess() {
        val sca = SCARulesDTO(listOf(RulesDTO("rule1", "description1")), listOf(RulesDTO("rule1", "description1")))
        whenever(
            snippetStatusRepository.updateStatusByUserEmail(
                "test@test.com",
                SnippetStatusEnum.COMPLIANT,
            ),
        ).thenReturn(1)
        doNothing().`when`(printScriptService).analyzeAllSnippets(sca, jwt)

        val result = snippetManagerService.updateSnippetSCA(sca, jwt)

        assertEquals(Unit, result)
    }

    @Test
    fun test022_updateSnippetSCAFail() {
        val sca = SCARulesDTO(listOf(RulesDTO("rule1", "description1")), listOf(RulesDTO("rule1", "description1")))
        whenever(
            snippetStatusRepository.updateStatusByUserEmail(
                "test2@test2.com",
                SnippetStatusEnum.COMPLIANT,
            ),
        ).thenReturn(0)
        doThrow(RuntimeException("Custom exception message")).`when`(printScriptService).analyzeAllSnippets(sca, jwt)

        val exception =
            assertThrows<Exception> {
                snippetManagerService.updateSnippetSCA(sca, jwt)
            }

        assertEquals("Custom exception message", exception.message)
    }

    @Test
    fun test023_updateSnippetFormatSuccess() {
        val format =
            FormatRulesDTO(listOf(RulesDTO("rule1", "description1")), listOf(RulesDTO("rule1", "description1")))
        whenever(
            snippetStatusRepository.updateStatusByUserEmail(
                "test@test.com",
                SnippetStatusEnum.COMPLIANT,
            ),
        ).thenReturn(1)
        doNothing().`when`(printScriptService).formatAllSnippets(format, jwt)

        val result = snippetManagerService.updateSnippetFormat(format, jwt)

        assertEquals(Unit, result)
    }

    @Test
    fun test024_updateSnippetFormatFail() {
        val format =
            FormatRulesDTO(listOf(RulesDTO("rule1", "description1")), listOf(RulesDTO("rule1", "description1")))
        whenever(
            snippetStatusRepository.updateStatusByUserEmail(
                "test@test.com",
                SnippetStatusEnum.COMPLIANT,
            ),
        ).thenReturn(0)
        doThrow(RuntimeException("Custom exception message")).`when`(printScriptService).formatAllSnippets(format, jwt)

        val exception =
            assertThrows<Exception> {
                snippetManagerService.updateSnippetFormat(format, jwt)
            }

        assertEquals("Custom exception message", exception.message)
    }

    @Test
    fun test025_deleteSnippetSuccess() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test@test.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(assetService.deleteSnippetFromBucket(snippetId)).thenReturn(Mono.empty())
        doNothing().`when`(snippetRepository).deleteById(snippetId)

        val result = snippetManagerService.deleteSnippet(snippetId, jwt)

        assertEquals(Unit, result)
    }

    @Test
    fun test026_deleteSnippetNotFoundFail() {
        val snippetId = 0L
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        val exception =
            assertThrows<Exception> {
                snippetManagerService.deleteSnippet(snippetId, jwt)
            }

        assertEquals("Snippet not found", exception.message)
    }

    @Test
    fun test027_deleteSnippetUnauthorizedFail() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test2@test2.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))

        val exception =
            assertThrows<Exception> {
                snippetManagerService.deleteSnippet(snippetId, jwt)
            }

        assertEquals("Unauthorized", exception.message)
    }

    @Test
    fun test028_deleteSnippetWithSharedSnippetSuccess() {
        val snippetId = 0L
        val mockedSnippet = Snippet("test", "java", "test2@test2.com")
        whenever(snippetRepository.findById(snippetId)).thenReturn(Optional.of(mockedSnippet))
        whenever(sharedSnippetRepository.findBySnippetIdAndUserEmail(snippetId, "test@test.com")).thenReturn(true)
        doNothing().`when`(sharedSnippetRepository).deleteBySnippetIdAndUserEmail(snippetId, "test@test.com")
        whenever(
            snippetStatusRepository.findBySnippetIdAndUserEmail(
                snippetId,
                "test@test.com",
            ),
        ).thenReturn(Optional.of(SnippetStatus("test@test.com", mockedSnippet, SnippetStatusEnum.PENDING)))
        whenever(assetService.deleteSnippetFromBucket(snippetId)).thenReturn(Mono.empty())
        doNothing().`when`(snippetRepository).deleteById(snippetId)

        val result = snippetManagerService.deleteSnippet(snippetId, jwt)

        assertEquals(Unit, result)
    }

    @Test
    fun test029_getFileTypesSuccess() {
        val fileTypes = snippetManagerService.getFileTypes()

        assertEquals(
            listOf(FileTypeDTO("Java", "java"), FileTypeDTO("Python", "py"), FileTypeDTO("Go", "go"), FileTypeDTO("Printscript", "prs")),
            fileTypes,
        )
    }
}

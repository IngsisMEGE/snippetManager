package printscript.snippetManager.controller

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.repository.SnippetStatusRepository
import printscript.snippetManager.service.interfaces.AssetService
import printscript.snippetManager.service.interfaces.SnippetManagerService
import reactor.core.publisher.Mono

@SpringBootTest
@AutoConfigureMockMvc
class SnippetManagerControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var snippetManagerService: SnippetManagerService

    @Autowired
    lateinit var snippetRepository: SnippetRepository

    @Autowired
    lateinit var snippetStatusRepository: SnippetStatusRepository

    @MockBean
    lateinit var assetService: AssetService

    @MockBean
    private lateinit var securityFilterChain: SecurityFilterChain

    @MockBean
    private lateinit var jwtDecoder: JwtDecoder

    private val testJwt = "test"

    @BeforeEach
    fun logIn() {
        val jwt =
            Jwt.withTokenValue(testJwt)
                .header("alg", "RS256")
                .claim("email", "test@test.com")
                .build()
        `when`(jwtDecoder.decode(testJwt)).thenReturn(jwt)

        val authorities = listOf(SimpleGrantedAuthority("SCOPE_write:snippets"))

        val authentication =
            TestingAuthenticationToken(
                jwt,
                "testPassword",
                authorities,
            )

        SecurityContextHolder.getContext().authentication = authentication

        `when`(securityFilterChain.matches(any(HttpServletRequest::class.java)))
            .thenReturn(true)
    }

    @Test
    fun test001_createSnippetInEditorWithAuthenticationShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test002_createSnippetInEditorWithoutPayloadParametersShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "language": "PrintScript",
                        "code": "let a: number = 5;"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test003_createSnippetInEditorWithEmptyPayloadParametersShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test004_editSnippetInEditorWithAuthenticationShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )

        logIn()
        mockMvc.perform(
            post("/snippetManager/edit/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "code": "let a: number = 10;"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test005_editSnippetInEditorWithoutPayloadParametersShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/edit/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test006_editSnippetInEditorWithEmptyPayloadParametersShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/edit/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "code": ""
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test007_getSnippetShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )

        whenever(assetService.getSnippetFromBucket(1)).thenReturn("let a: number = 5;")

        logIn()
        mockMvc.perform(
            get("/snippetManager/get/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test008_getSnippetWithInvalidIdShouldReturnBadRequest() {
        mockMvc.perform(
            get("/snippetManager/get/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test009_shareSnippetShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )

        logIn()
        mockMvc.perform(
            post("/snippetManager/share/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .param("shareEmail", "test3@test3.com"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test010_shareSnippetWithInvalidIdShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/share/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .param("shareEmail", "test2@test2.com"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test012_shareSnippetTwiceWithSameEmailShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )

        logIn()
        mockMvc.perform(
            post("/snippetManager/share/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .param("shareEmail", "test2@test2.com"),
        )

        logIn()
        mockMvc.perform(
            post("/snippetManager/share/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .param("shareEmail", "test2@test2.com"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test013_shareOwnSnippetShouldReturnBadRequest() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )

        logIn()
        mockMvc.perform(
            post("/snippetManager/share/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .param("shareEmail", "test@test.com"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test014_searchSnippetsShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/search")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "language": "PrintScript",
                        "permission": ""
                    }
                    """.trimIndent(),
                )
                .param("page", "0")
                .param("size", "10"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test015_deleteSnippetShouldReturnOk() {
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )
        logIn()
        mockMvc.perform(
            post("/snippetManager/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt")
                .content(
                    """
                    {
                        "name": "snippet",
                        "language": "PrintScript",
                        "code": "let a: number = 5;",
                        "extension": "prs"
                    }
                    """.trimIndent(),
                ),
        )
        logIn()

        whenever(assetService.deleteSnippetFromBucket(2)).thenReturn(Mono.empty())
        mockMvc.perform(
            delete("/snippetManager/delete/2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun test016_deleteSnippetWithInvalidIdShouldReturnBadRequest() {
        mockMvc.perform(
            delete("/snippetManager/delete/99")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun test017_getFileTypesShouldReturnOk() {
        mockMvc.perform(
            get("/snippetManager/fileTypes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $testJwt"),
        )
            .andExpect(status().isOk)
    }
}

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import printscript.snippetManager.repository.SnippetRepository
import printscript.snippetManager.repository.SnippetStatusRepository
import printscript.snippetManager.service.interfaces.AssetService
import printscript.snippetManager.service.interfaces.SnippetManagerService

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
    fun setUp() {
        val jwt =
            Jwt.withTokenValue(testJwt)
                .header("alg", "RS256") // Add the algorithm header (you may adjust this based on your JWT)
                .claim("email", "test@test.com") // Extract other claims as needed
                .build()
        `when`(jwtDecoder.decode(testJwt)).thenReturn(jwt)

        val authorities = listOf(SimpleGrantedAuthority("SCOPE_write:snippets"))

        // Create a mock Authentication with the JWT
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
                        "code": "let a: number = 5;"
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
                        "code": "let a: number = 5;"
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
                        "code": "let a: number = 5;"
                    }
                    """.trimIndent(),
                ),
        )

        setUp()
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
                        "code": "let a: number = 5;"
                    }
                    """.trimIndent(),
                ),
        )

        whenever(assetService.getSnippetFromBucket(1)).thenReturn("let a: number = 5;")

        setUp()
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
}

package printscript.snippetManager.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
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

    private val testJwt =
        "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ildmak1Jb3BXb2lDQzNydFN5azM2UiJ9." +
            "eyJuaWNrbmFtZSI6InRlc3QiLCJuYW1lIjoidGVzdEB0ZXN0LmNvbSIsInBpY3R1cmUiOiJ" +
            "odHRwczovL3MuZ3JhdmF0YXIuY29tL2F2YXRhci9iNjQyYjQyMTdiMzRiMWU4ZDNiZDkxNWZjNj" +
            "VjNDQ1Mj9zPTQ4MCZyPXBnJmQ9aHR0cHMlM0ElMkYlMkZjZG4uYXV0aDAuY29tJTJGYXZhdGFycyUy" +
            "RnRlLnBuZyIsInVwZGF0ZWRfYXQiOiIyMDI0LTA2LTIyVDAwOjI2OjM5LjY3NVoiLCJlbWFpbCI6InRlc" +
            "3RAdGVzdC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh0dHBzOi8vZGV2LTBldDNjZXdoc" +
            "Go1aGx0d24udXMuYXV0aDAuY29tLyIsImF1ZCI6IlY2QkszZWtVUjlySDlhVjM5bGtTWVJXQmhFV2dSMmtRIiwia" +
            "WF0IjoxNzE5MDE2MDAwLCJleHAiOjE3MTkwNTIwMDAsInN1YiI6ImF1dGgwfDY2NzVkMjczYTAwMDZiMjkxZDk1ZTY" +
            "yMyIsInNpZCI6IjJiU2U2c2p4S3JEamZfTTR5UThCX0IzVDVqNjh1dDhOIiwibm9uY2UiOiJMbTl3UmpKRlYydGtOSE5KYmp" +
            "kME5tdDFRelp1VG1JMlEzZGxiVmd6VmtveFVGaHRZaTV6YTBkWE9RPT0ifQ.c5DncfTQ06xz3uM8E0VW4CDeZR_T120OH5oz7" +
            "8H0ByWUXoUvqXaE9L6_9OZVm3mK-zkFtwzhN74nNUHW38v4-hn4awnijcI5uMFfBNW01WU5B8VBd1fbVm61Bxv4J-eEPex9-_s5" +
            "-tPXMshx4InYtQKkvMXhXjS8-OLvD0SPT-2U8ZJkZPGakC0h8L5PWNamRsxJhIq2cE8wHEmwvdPbLV51iiT9QMD7NXNzW8R5y1Ot" +
            "FarKa_ZkIpiUy3wTESqSU3qbjUS8QnlYH_wrMpajCPlsKv0dzqv9NTKjcdmGctnyPP-nOp60XRG9T_RD5rZRthrU0-GU3ecV" +
            "-Jn5tYFHvw"

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
}

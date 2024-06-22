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
            "odHRwczovL3MuZ3JhdmF0YXIuY29tL2F2YXRhci9iNjQyYjQyMTdiMzRiMWU4ZDNiZDkxNW" +
            "ZjNjVjNDQ1Mj9zPTQ4MCZyPXBnJmQ9aHR0cHMlM0ElMkYlMkZjZG4uYXV0aDAuY29tJTJGYX" +
            "ZhdGFycyUyRnRlLnBuZyIsInVwZGF0ZWRfYXQiOiIyMDI0LTA2LTIyVDAwOjI2OjM5LjY3NVoi" +
            "LCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImlzcyI6Imh" +
            "0dHBzOi8vZGV2LTBldDNjZXdocGo1aGx0d24udXMuYXV0aDAuY29tLyIsImF1ZCI6IlY2QkszZWtVU" +
            "jlySDlhVjM5bGtTWVJXQmhFV2dSMmtRIiwiaWF0IjoxNzE5MDkyMTAxLCJleHAiOjE3MTkxMjgxMDEsI" +
            "nN1YiI6ImF1dGgwfDY2NzVkMjczYTAwMDZiMjkxZDk1ZTYyMyIsInNpZCI6IjJiU2U2c2p4S3JEamZfTTR5" +
            "UThCX0IzVDVqNjh1dDhOIiwibm9uY2UiOiJaSE5oWVVwbWJtZEROQzVCYW1kQ2JWUk5hakIwZEhCZlR6UldjV" +
            "zl0WVZKUlRVMUNMVzAzWTB4ZmN3PT0ifQ.TH-OjAg9_iQIAyferbi1w-T4WLjqpDoXyJmRuuB1vqjtAk4cUSj1d" +
            "X7PrkrPCIfkJ6nLKCWvf9NHGKQOz88XbSuuKBZptVtZ8ktF7I_6aaxdP88n57jrb_KpQUtT-MfDJdjukyd3_WbYS" +
            "oRxeR1pxI85ZSiMrpqI054j9Hrh8ycEQ_rwrRegAwpldE1JTdBjhmInl2wY-dQ33-FN31bBVYmQRUGGo4WY8uadCDw6XO" +
            "hkpx5D9yjfFwKa8zvC_zBIE3_DPt9BWjJYSrjuH6lQbomEprlNRVaNSp9Jr558iKq6SgsYkM0jD9ABaDOZyc-NQH5_PK_8DRJHhTN2EMTGMA"

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
}

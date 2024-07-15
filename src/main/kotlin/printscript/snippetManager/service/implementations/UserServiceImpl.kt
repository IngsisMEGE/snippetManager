package printscript.snippetManager.service.implementations

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import printscript.snippetManager.controller.payload.response.UserDTO
import printscript.snippetManager.service.interfaces.UserService

@Service
class UserServiceImpl(
    private val webClient: WebClient,
    @Autowired private val dotenv: Dotenv,
) : UserService {
    private val auth0Url = "${dotenv["AUTH_SERVER_URI"]}/api/v2"

    override fun getUsers(
        userData: Jwt,
        page: Int,
        pageSize: Int,
    ): Page<UserDTO> {
        val pageable = PageRequest.of(page, pageSize)

        val users =
            webClient.get()
                .uri("$auth0Url/users?fields=email,nickname&page=$page&per_page=$pageSize")
                .header("Authorization", "Bearer ${dotenv["AUTH0_MANAGEMENT_API_TOKEN"]}")
                .retrieve()
                .bodyToMono<List<UserDTO>>()
                .block()!!

        return PageImpl(users.filter { it.email != userData.claims["email"] }, pageable, users.size.toLong())
    }
}

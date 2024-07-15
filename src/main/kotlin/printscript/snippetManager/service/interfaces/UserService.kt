package printscript.snippetManager.service.interfaces
import org.springframework.data.domain.Page
import org.springframework.security.oauth2.jwt.Jwt
import printscript.snippetManager.controller.payload.response.UserDTO

interface UserService {
    fun getUsers(
        userData: Jwt,
        page: Int,
        pageSize: Int,
    ): Page<UserDTO>
}

package printscript.snippetManager.controller

import org.springframework.data.domain.Page
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import printscript.snippetManager.controller.payload.response.UserDTO
import printscript.snippetManager.service.interfaces.UserService

@RestController
@RequestMapping("/user")
class UserController(val userService: UserService) {
    @GetMapping("/get")
    fun getUser(
        @AuthenticationPrincipal userData: Jwt,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "") name: String,
    ): Page<UserDTO> {
        return userService.getUsers(userData, page, size, name)
    }
}

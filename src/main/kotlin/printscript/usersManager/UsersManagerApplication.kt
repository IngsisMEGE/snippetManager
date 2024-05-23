package printscript.usersManager

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
class UsersManagerApplication {
    @GetMapping("/")
    fun index(): String {
        return "I'm Alive!"
    }

    @GetMapping("/jwt")
    fun jwt(
        @AuthenticationPrincipal jwt: Jwt,
    ): String {
        return jwt.tokenValue
    }

    @GetMapping("/snippets")
    fun getAllMessages(): String {
        return "secret message"
    }

    @GetMapping("/snippets/{id}")
    fun getSingleMessage(
        @PathVariable id: String,
    ): String {
        return "secret message $id"
    }

    @PostMapping("/snippets")
    fun createMessage(
        @RequestBody message: String?,
    ): String {
        return String.format("Message was created. Content: %s", message)
    }
}

fun main(args: Array<String>) {
    val dotenv = Dotenv.load()
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<UsersManagerApplication>(*args)
}

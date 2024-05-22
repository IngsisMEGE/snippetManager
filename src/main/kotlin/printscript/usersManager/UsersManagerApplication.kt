package printscript.usersManager

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class UsersManagerApplication{
    @Bean
    fun dotenv(): Dotenv {
        return Dotenv.configure().load()
    }
}

fun main(args: Array<String>) {
    runApplication<UsersManagerApplication>(*args)
}

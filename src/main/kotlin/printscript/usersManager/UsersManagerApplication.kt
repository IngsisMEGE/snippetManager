package printscript.usersManager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UsersManagerApplication

fun main(args: Array<String>) {
	runApplication<UsersManagerApplication>(*args)
}

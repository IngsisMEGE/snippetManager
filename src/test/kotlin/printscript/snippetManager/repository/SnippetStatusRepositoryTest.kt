package printscript.snippetManager.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import printscript.snippetManager.entity.Snippet
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.enums.SnippetStatusEnum

@DataJpaTest
class SnippetStatusRepositoryTest {
    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var snippetStatusRepository: SnippetStatusRepository

    @Test
    fun test001UpdateStatusByUser() {
        // Arrange
        val snippet =
            Snippet(
                name = "Test Snippet",
                language = "Kotlin",
                author = "Author Name",
            )
        entityManager.persist(snippet)
        entityManager.flush()

        val userEmail = "test@example.com"
        val snippetStatus1 = SnippetStatus(userEmail, snippet, SnippetStatusEnum.COMPLIANT)
        val snippetStatus2 = SnippetStatus(userEmail, snippet, SnippetStatusEnum.NOT_COMPLIANT)

        entityManager.persist(snippetStatus1)
        entityManager.persist(snippetStatus2)
        entityManager.flush()

        // Act
        val updatedCount = snippetStatusRepository.updateStatusByUserEmail(userEmail, SnippetStatusEnum.PENDING)
        entityManager.flush()
        entityManager.clear()

        // Assert
        assertEquals(2, updatedCount)

        val updatedStatuses = snippetStatusRepository.findAll()
        assertEquals(2, updatedStatuses.size)
        updatedStatuses.forEach {
            assertEquals(SnippetStatusEnum.PENDING, it.status)
        }
    }
}

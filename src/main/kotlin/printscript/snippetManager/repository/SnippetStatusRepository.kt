package printscript.snippetManager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import printscript.snippetManager.entity.SnippetStatus
import java.util.Optional

@Repository
interface SnippetStatusRepository : JpaRepository<SnippetStatus, Long> {
    @Query("SELECT s FROM SnippetStatus s WHERE s.snippet.id = :snippetId AND s.userEmail = :email")
    fun findBySnippetIdAndUserEmail(
        snippetId: Long,
        email: String,
    ): Optional<SnippetStatus>
}

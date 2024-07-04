package printscript.snippetManager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.enums.SnippetStatusEnum
import java.util.Optional

@Repository
interface SnippetStatusRepository : JpaRepository<SnippetStatus, Long> {
    @Query("SELECT s FROM SnippetStatus s WHERE s.snippet.id = :snippetId AND s.userEmail = :email")
    fun findBySnippetIdAndUserEmail(
        snippetId: Long,
        email: String,
    ): Optional<SnippetStatus>

    @Transactional
    @Modifying
    @Query("UPDATE SnippetStatus s SET s.status = :status WHERE s.userEmail = :userEmail")
    fun updateStatusByUserEmail(
        userEmail: String,
        status: SnippetStatusEnum,
    ): Int
}

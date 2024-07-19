package printscript.snippetManager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import printscript.snippetManager.entity.SharedSnippet

@Repository
interface SharedSnippetRepository : JpaRepository<SharedSnippet, Long> {
    @Query("SELECT COUNT(s) > 0 FROM SharedSnippet s WHERE s.snippet.id = :snippetId AND s.userEmail = :email")
    fun findBySnippetIdAndUserEmail(
        snippetId: Long,
        email: String,
    ): Boolean

    @Transactional
    @Modifying
    @Query("DELETE FROM SharedSnippet s WHERE s.snippet.id = :snippetId AND s.userEmail = :email")
    fun deleteBySnippetIdAndUserEmail(
        snippetId: Long,
        email: String,
    )
}

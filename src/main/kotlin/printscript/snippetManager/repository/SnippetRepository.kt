package printscript.snippetManager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import printscript.snippetManager.entity.Snippet

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long>{

    @Query("SELECT s FROM Snippet s WHERE s.author = :author")
    fun getSnippetsByAuthor(author : String) : List<Snippet>
}

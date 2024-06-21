package printscript.snippetManager.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import printscript.snippetManager.entity.Snippet

@Repository
interface SnippetRepository : JpaRepository<Snippet, Long>

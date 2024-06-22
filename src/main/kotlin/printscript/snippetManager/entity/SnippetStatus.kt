package printscript.snippetManager.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import printscript.snippetManager.enums.SnippetStatus

@Entity
data class SnippetStatus(
    val userEmail: String,
    @ManyToOne
    val snippet: Snippet,
    var status: SnippetStatus,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}

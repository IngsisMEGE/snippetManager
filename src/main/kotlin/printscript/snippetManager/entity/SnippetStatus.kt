package printscript.snippetManager.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import printscript.snippetManager.enums.SnippetStatusEnum

@Entity
data class SnippetStatus(
    val userEmail: String,
    @ManyToOne
    val snippet: Snippet,
    var status: SnippetStatusEnum,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}

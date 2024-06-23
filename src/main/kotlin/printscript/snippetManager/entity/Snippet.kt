package printscript.snippetManager.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
data class Snippet(
    val name: String,
    val language: String,
    val author: String,
    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sharedSnippets: List<SharedSnippet> = mutableListOf(),
    @OneToMany(mappedBy = "snippet", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val snippetStatus: List<SnippetStatus> = mutableListOf(),
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}

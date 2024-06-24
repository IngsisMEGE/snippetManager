package printscript.snippetManager.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import printscript.snippetManager.controller.payload.request.FilterDTO
import printscript.snippetManager.controller.payload.response.SnippetViewDTO
import printscript.snippetManager.entity.SharedSnippet
import printscript.snippetManager.entity.Snippet
import printscript.snippetManager.entity.SnippetStatus
import printscript.snippetManager.enums.Permission
import printscript.snippetManager.enums.SnippetStatusEnum

@Repository
class FilterRepository(private val em: EntityManager) {
    fun filterSnippets(
        filter: FilterDTO,
        pageable: Pageable,
        userMail: String,
    ): Page<SnippetViewDTO> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(SnippetViewDTO::class.java)
        val root = cq.from(Snippet::class.java)

        criteriaBuilderFilter(cb, root, filter, cq, userMail)

        val typedQuery = em.createQuery(cq)

        val totalRows = typedQuery.resultList.size
        typedQuery.firstResult = pageable.pageNumber * pageable.pageSize
        typedQuery.maxResults = pageable.pageSize

        return PageImpl(typedQuery.resultList, pageable, totalRows.toLong())
    }

    private fun criteriaBuilderFilter(
        cb: CriteriaBuilder,
        root: Root<Snippet>,
        filter: FilterDTO,
        cq: CriteriaQuery<SnippetViewDTO>,
        userMail: String,
    ) {
        val predicates = mutableListOf<Predicate>()

        val sharedSnippetJoin: Join<Snippet, SharedSnippet> = root.join("sharedSnippets", JoinType.LEFT)
        val snippetStatusJoin: Join<Snippet, SnippetStatus> = root.join("snippetStatus", JoinType.LEFT)

        if (filter.language.isNotBlank()) {
            predicates.add(cb.like(cb.lower(root.get("language")), "%" + filter.language.lowercase() + "%"))
        }

        if (filter.permission.isNotBlank()) {
            when (Permission.valueOf(filter.permission.uppercase())) {
                Permission.OWNER -> predicates.add(cb.equal(root.get<String>("author"), userMail))
                Permission.SHARED ->
                    predicates.add(
                        cb.and(
                            cb.notEqual(root.get<String>("author"), userMail),
                            cb.equal(sharedSnippetJoin.get<String>("userEmail"), userMail),
                        ),
                    )
            }
        } else {
            val ownerPredicate = cb.equal(root.get<String>("author"), userMail)
            val sharedPredicate = cb.equal(sharedSnippetJoin.get<String>("userEmail"), userMail)
            predicates.add(cb.or(ownerPredicate, sharedPredicate))
        }

        cq.select(
            cb.construct(
                SnippetViewDTO::class.java,
                root.get<Long>("id"),
                root.get<String>("name"),
                root.get<String>("language"),
                root.get<String>("author"),
                cb.selectCase<SnippetStatusEnum, String>(snippetStatusJoin.get("status"))
                    .`when`(SnippetStatusEnum.PENDING, "PENDING")
                    .`when`(SnippetStatusEnum.COMPLIANT, "COMPLIANT")
                    .`when`(SnippetStatusEnum.NOT_COMPLIANT, "NOT_COMPLIANT")
                    .otherwise("UNKNOWN"),
            ),
        ).where(*predicates.toTypedArray())
    }
}

package com.floristeriaakasia.backend.feature.floralArrangment.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface FloralArrangementRepository : JpaRepository<FloralArrangement, Long> {
    fun existsBySlug(slug: String): Boolean

    fun findBySlug(slug: String):FloralArrangement

    @Modifying
    @Query("UPDATE FloralArrangement fa SET fa.views = fa.views + 1 WHERE fa.id = :id")
    fun incrementViews(@Param("id") id: Long)

    @EntityGraph(attributePaths = ["price", "description", "categories", "tags", "flowers", "gallery"])
    @Query("SELECT fa FROM FloralArrangement fa WHERE fa.id = :id")
    fun findByIdWithDetails(@Param("id") id: Long): FloralArrangement?
    fun id(id: Long): MutableList<FloralArrangement>

    /**
     * Paginated list — loads only scalar columns + price.
     * Gallery, flowers, and tags are loaded separately to avoid
     * the HibernateJpaDialect "HHH90003004" firstResult/maxResults
     * warning that appears when pagination is mixed with collection fetches.
     */
    @Query(
        value = """
            SELECT DISTINCT fa FROM FloralArrangement fa
            JOIN FETCH fa.price p
            LEFT JOIN fa.categories c
            LEFT JOIN fa.tags t
            WHERE (:categoryId  IS NULL OR c.id     = :categoryId)
              AND (:tagId        IS NULL OR t.id     = :tagId)
              AND (:featured     IS NULL OR fa.featured   = :featured)
              AND (:seasonal     IS NULL OR fa.seasonal   = :seasonal)
              AND (:isAvailable  IS NULL OR fa.isAvailable = :isAvailable)
              AND (:minPrice     IS NULL OR p.price  >= :minPrice)
              AND (:maxPrice     IS NULL OR p.price  <= :maxPrice)
        """,
        countQuery = """
            SELECT COUNT(DISTINCT fa.id) FROM FloralArrangement fa
            LEFT JOIN fa.categories c
            LEFT JOIN fa.tags t
            JOIN fa.price p
            WHERE (:categoryId  IS NULL OR c.id     = :categoryId)
              AND (:tagId        IS NULL OR t.id     = :tagId)
              AND (:featured     IS NULL OR fa.featured   = :featured)
              AND (:seasonal     IS NULL OR fa.seasonal   = :seasonal)
              AND (:isAvailable  IS NULL OR fa.isAvailable = :isAvailable)
              AND (:minPrice     IS NULL OR p.price  >= :minPrice)
              AND (:maxPrice     IS NULL OR p.price  <= :maxPrice)
        """
    )
    fun findAllWithFilters(
        @Param("categoryId") categoryId: Long?,
        @Param("tagId") tagId: Long?,
        @Param("featured") featured: Boolean?,
        @Param("seasonal") seasonal: Boolean?,
        @Param("isAvailable") isAvailable: Boolean?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<FloralArrangement>

    @Query("""
        SELECT fa FROM FloralArrangement fa
        JOIN FETCH fa.categories
        WHERE fa.id IN :ids
    """)
    fun findWithCategoriesByIds(@Param("ids") ids: Collection<Long>): List<FloralArrangement>

    @Query("""
        SELECT fa FROM FloralArrangement fa
        JOIN FETCH fa.tags
        WHERE fa.id IN :ids
    """)
    fun findWithTagsByIds(@Param("ids") ids: Collection<Long>): List<FloralArrangement>

    @Query("""
        SELECT g FROM ProductGallery g
        WHERE g.floralArrangement.id IN :ids
          AND g.isPrimary = true
    """)
    fun findPrimaryImagesByArrangementIds(
        @Param("ids") ids: Collection<Long>
    ): List<ProductGallery>
}
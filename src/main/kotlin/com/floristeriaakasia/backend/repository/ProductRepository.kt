package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.StockStatus
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySlug(slug: String): Product?
    fun findByStatus(status: Boolean): List<Product>

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        JOIN FETCH p.subCategory
        WHERE p.status = true
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:subcategoryId IS NULL OR p.subCategory.id = :subcategoryId)
        AND (:featured IS NULL OR p.featured = :featured)
        AND (:seasonal IS NULL OR p.seasonal = :seasonal)
    """)
    fun findWithFilters(
        @Param("categoryId") categoryId: Long?,
        @Param("subcategoryId") subcategoryId: Long?,
        @Param("featured") featured: Boolean?,
        @Param("seasonal") seasonal: Boolean?,
        pageable: Pageable
    ): Page<Product>

    @Modifying
    @Query("UPDATE Product p SET p.views = p.views + 1 WHERE p.id = :id")
    fun incrementViews(@Param("id") id: Long)
}
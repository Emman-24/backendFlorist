package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByRoute(route: String): Product?
    fun findByStatus(status: Boolean): List<Product>
    fun findByCategoryAndStatus(category: Category, status: Boolean): List<Product>
    fun findBySubCategoryAndStatus(subCategory: SubCategory, status: Boolean): List<Product>
    fun findByFeaturedTrueAndStatusOrderByCreatedAtDesc(status: Boolean): List<Product>
    fun findByStockStatusAndStatus(stockStatus: String, status: Boolean): List<Product>

    fun countByStatus(status: Boolean): Int
    fun countByFeatured(featured: Boolean): Int
    fun countBySeasonal(seasonal: Boolean): Int

    @Query("SELECT COALESCE(SUM(p.views), 0) FROM Product p")
    fun sumViews(): Int?

    @Query("SELECT AVG(p.price) FROM Product p")
    fun averagePrice(): BigDecimal?

//    @Query("""
//        SELECT p FROM Product p
//        WHERE p.status = :status
//        ORDER BY p.views DESC
//    """)
//    fun findTopViewedProducts(@Param("status") status: Int): List<Product>
//
//    @Query("""
//        SELECT p FROM Product p
//        JOIN p.tags t
//        WHERE t.id IN :tagIds
//        AND p.status = :status
//        GROUP BY p.id
//        HAVING COUNT(DISTINCT t.id) >= :minMatches
//    """)
//    fun findByTagsIn(
//        @Param("tagIds") tagIds: List<Long>,
//        @Param("minMatches") minMatches: Long,
//        @Param("status") status: Int
//    ): List<Product>
//
}
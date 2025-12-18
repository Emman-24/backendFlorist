package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductSeoContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductSeoContentRepository: JpaRepository<ProductSeoContent, Long> {
    fun findByProductOrderByPositionAsc(product: Product): List<ProductSeoContent>

    fun findByProductAndContentType(product: Product, contentType: String): List<ProductSeoContent>

    @Query("""
        SELECT p FROM ProductSeoContent p 
        WHERE p.product.id = :productId 
        AND p.status = :status 
        ORDER BY p.position ASC
    """)
    fun findActiveByProductId(
        @Param("productId") productId: Long,
        @Param("status") status: Int
    ): List<ProductSeoContent>
}
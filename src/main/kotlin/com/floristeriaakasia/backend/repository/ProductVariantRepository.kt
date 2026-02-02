package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductVariantRepository: JpaRepository<ProductVariant, Long> {
    fun findByProductOrderByPositionAsc(product: Product): List<ProductVariant>

    fun findByProductAndVariantType(product: Product, variantType: String): List<ProductVariant>

    @Query("""
        SELECT v FROM ProductVariant v 
        WHERE v.product.id = :productId 
        AND v.available = true 
        AND v.status = :status
        ORDER BY v.position ASC
    """)
    fun findAvailableByProductId(
        @Param("productId") productId: Long,
        @Param("status") status: Int
    ): List<ProductVariant>
}
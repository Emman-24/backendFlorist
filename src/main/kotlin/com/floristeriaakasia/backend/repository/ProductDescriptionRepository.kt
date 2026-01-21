package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductDescription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductDescriptionRepository : JpaRepository<ProductDescription, Long> {
    fun findByProductOrderByPositionAsc(product: Product): List<ProductDescription>
    fun countByProduct(product: Product): Long
    fun deleteByProduct(product: Product)

    @Query("SELECT d FROM ProductDescription d WHERE d.product.id = :productId ORDER BY d.position ASC")
    fun findByProductIdOrderByPositionAsc(@Param("productId") productId: Long): List<ProductDescription>

}
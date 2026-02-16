package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductGallery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductGalleryRepository : JpaRepository<ProductGallery, Long> {
    fun findByProductOrderByPositionAsc(product: Product): List<ProductGallery>

    fun findByProductAndIsPrimaryTrue(product: Product): ProductGallery?

    @Query(
        """
        SELECT g FROM ProductGallery g 
        WHERE g.product.id = :productId 
        AND g.status = :status 
        ORDER BY g.position ASC
    """
    )
    fun findActiveByProductId(
        @Param("productId") productId: Long,
        @Param("status") status: Int
    ): List<ProductGallery>

}
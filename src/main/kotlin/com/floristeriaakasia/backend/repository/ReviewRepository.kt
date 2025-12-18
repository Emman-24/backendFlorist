package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository: JpaRepository<Review, Long> {
    fun findByProductAndStatus(product: Product, status: String): List<Review>

    @Query("""
        SELECT r FROM Review r 
        WHERE r.product.id = :productId 
        AND r.status = 'approved'
        ORDER BY r.createdAt DESC
    """)
    fun findApprovedByProductId(@Param("productId") productId: Long): List<Review>

    @Query("""
        SELECT AVG(r.rating) FROM Review r 
        WHERE r.product.id = :productId 
        AND r.status = 'approved'
    """)
    fun getAverageRatingByProductId(@Param("productId") productId: Long): Double?

    @Query("""
        SELECT COUNT(r) FROM Review r 
        WHERE r.product.id = :productId 
        AND r.status = 'approved'
    """)
    fun getReviewCountByProductId(@Param("productId") productId: Long): Long

    fun findByStatusOrderByCreatedAtDesc(status: String): List<Review>
}
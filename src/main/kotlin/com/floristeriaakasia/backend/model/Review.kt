package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener::class)
class Review(
    var customerName: String = "",
    var customerEmail: String? = null,
    var rating: Int = 0,
    var comment: String = "",
    var verifiedPurchase: Boolean = false,
    var helpfulCount: Int = 0,
    var status: ReviewStatus = ReviewStatus.PENDING
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    lateinit var product: Product

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    var publishedAt: Instant? = null

    init {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
    }
}

enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}
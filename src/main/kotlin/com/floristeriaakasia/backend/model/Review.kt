package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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

@Entity
@Table(
    name = "reviews",
    indexes = [
        Index(name = "idx_reviews_product", columnList = "product_id, status"),
        Index(name = "idx_reviews_rating", columnList = "rating")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Review(
    @Column(name = "customer_name", length = 100, nullable = false)
    var customerName: String = "",

    @Column(name = "customer_email")
    var customerEmail: String? = null,

    @Column(nullable = false)
    var rating: Int = 1,

    @Column(columnDefinition = "TEXT", nullable = false)
    var comment: String = "",

    @Column(name = "verified_purchase", nullable = false)
    var verifiedPurchase: Boolean = false,

    @Column(name = "helpful_count", nullable = false)
    var helpfulCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: ReviewStatus = ReviewStatus.PENDING
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "published_at")
    var publishedAt: Instant? = null

    init {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
    }
}

enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED;

    override fun toString(): String = name.lowercase()
}
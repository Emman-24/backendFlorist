package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(
    name = "faqs", indexes = [
        Index(name = "idx_faqs_category", columnList = "category, position")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Faq(
    var question: String = "",
    var answer: String = "",
    var category: String = "",
    var position: Int = 0,
    var views: Int = 0,
    var helpfulCount: Int = 0,
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
}
package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(
    name = "seo_redirects",
    indexes = [
        Index(name = "idx_old_path", columnList = "oldPath", unique = true),
        Index(name = "idx_new_path", columnList = "newPath"),
        Index(name = "idx_entity", columnList = "entityType,entityId")
    ]
)
class SeoRedirect(
    @Column(nullable = false, unique = true, length = 500)
    val oldPath: String,

    @Column(nullable = false, length = 500)
    var newPath: String,

    @Column(nullable = false)
    val entityType: String, // "product", "category", "subcategory"

    @Column(nullable = false)
    val entityId: Long,

    @Column(nullable = false)
    val statusCode: Int = 301, // 301 (permanente) o 302 (temporal)

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false)
    var hitCount: Int = 0,

    @Column
    var lastHitAt: LocalDateTime? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()

    fun recordHit() {
        hitCount++
        lastHitAt = LocalDateTime.now()
    }
}
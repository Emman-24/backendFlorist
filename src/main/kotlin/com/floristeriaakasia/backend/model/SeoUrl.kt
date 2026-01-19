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
    name = "seo_urls", indexes = [
        Index(name = "idx_full_path", columnList = "fullPath", unique = true),
        Index(name = "idx_entity", columnList = "entityType,entityId", unique = true),
        Index(name = "idx_slug", columnList = "slug")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class SeoUrl(
    @Column(nullable = false, length = 50)
    var entityType: String = "",

    @Column(nullable = false)
    var entityId: Long = 0,

    @Column(nullable = false, length = 255, unique = true)
    var slug: String = "",

    @Column(nullable = false, length = 500, unique = true)
    var fullPath: String = "",

    @Column(length = 500)
    var canonicalUrl: String = "",

    @Column(nullable = false)
    var status: Boolean = true
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
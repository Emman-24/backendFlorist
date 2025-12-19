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
        Index(name = "idx_seo_urls_entity", columnList = "entity_type, entity_id"),
        Index(name = "idx_seo_urls_slug", columnList = "slug")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class SeoUrl(
    var entityType: String = "",
    var entityId: Long = 0,
    var slug: String = "",
    var fullPath: String = "",
    var canonicalUrl: String = "",
    var redirectFrom: String = "",
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
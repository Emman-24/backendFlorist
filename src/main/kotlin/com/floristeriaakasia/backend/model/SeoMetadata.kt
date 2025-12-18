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
    name = "seo_metadata", indexes = [
        Index(name = "idx_seo_metadata_entity", columnList = "entity_type, entity_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class SeoMetadata(
    var entityType: String = "",
    var entityId: Long = 0,
    var metaTitle: String = "",
    var metaDescription: String = "",
    var ogTitle: String = "",
    var ogDescription: String = "",
    var ogImage: String = "",
    var schemaMarkup: String = "",
    var h1Override: String = "",
    var noindex: Boolean = false,
    var nofollow: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
}
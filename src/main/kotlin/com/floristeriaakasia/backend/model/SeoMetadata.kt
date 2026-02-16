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
@Table(name = "seo_metadata")
@EntityListeners(AuditingEntityListener::class)
class SeoMetadata(

    /**
     * Product / category / subcategory
     */
    @Column(nullable = false)
    var entityType: String = "",

    @Column(nullable = false)
    var entityId: Long = 0,

    @Column(length = 60)
    var metaTitle: String = "",

    @Column(length = 320)
    var metaDescription: String = "",

    @Column(columnDefinition = "TEXT")
    var schemaMarkup: String? = null,

    @Column(nullable = false)
    var isCustom: Boolean = false,
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
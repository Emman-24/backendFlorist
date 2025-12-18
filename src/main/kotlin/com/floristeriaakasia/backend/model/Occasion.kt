package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "occasions", indexes = [
    Index(name = "idx_occasions_slug", columnList = "slug")
])
@EntityListeners(AuditingEntityListener::class)
class Occasion(
    var name: String = "",
    var slug: String = "",
    var description: String = "",
    var seoContent: String = "",
    var imageUrl: String = "",
    var dateSpecific: LocalDateTime = LocalDateTime.now(),
    var dateRangeStart: LocalDateTime = LocalDateTime.now(),
    var dateRangeEnd: LocalDateTime = LocalDateTime.now(),
    var isRecurring: Boolean = false,
    var position: Int = 0,
    var status: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToMany(mappedBy = "occasions")
    val products: MutableSet<Product> = mutableSetOf()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
}
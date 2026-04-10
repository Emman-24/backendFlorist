package com.floristeriaakasia.backend.feature.productDescription

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "product_descriptions")
@EntityListeners(AuditingEntityListener::class)
data class ProductDescription(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var shortDescription: String,

    @Column(nullable = false,columnDefinition = "TEXT")
    var description: String,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    val updatedAt: Instant = Instant.now()

)
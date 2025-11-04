package com.floristeriaakasia.backend.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategory_id", nullable = false)
    var subCategory: SubCategory,

    @Column(nullable = false, length = 255)
    var route: String,

    @Column(nullable = false)
    var status: Boolean = true,

    @Column(nullable = false, length = 255)
    var title: String,

    @ElementCollection
    @CollectionTable(name = "product_descriptions", joinColumns = [JoinColumn(name = "product_id")])
    @OrderColumn(name = "position")
    @Column(name = "paragraph", columnDefinition = "TEXT")
    var description: MutableList<String>? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false, length = 1024)
    var imagePath: String,

    @Column(length = 1024)
    var facebookUrl: String? = null,

    @Column(length = 1024)
    var instagramUrl: String? = null,

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()
)
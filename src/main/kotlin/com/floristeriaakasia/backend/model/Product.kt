package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "products", indexes = [
        Index(name = "idx_products_category", columnList = "category_id, status"),
        Index(name = "idx_products_subcategory", columnList = "subcategory_id, status"),
        Index(name = "idx_products_route", columnList = "route"),
        Index(name = "idx_products_featured", columnList = "featured, status")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Product(
    var title: String = "",
    var route: String = "",
    var fullRoute: String = "",
    var status: Boolean = true,
    var price: BigDecimal = BigDecimal.ZERO,
    var originalName: String = "",
    var storedName: String = "",
    var mimeType: String = "",
    var size: Long = Long.MIN_VALUE,
    var altText: String = "",
    var stockStatus: String = "available",
    var seasonal: Boolean = false,
    var featured: Boolean = false,
    var facebookUrl: String = "",
    var instagramUrl: String = "",
    var views: Int = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category = Category()

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcategory_id", nullable = false)
    var subCategory: SubCategory = SubCategory()

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "product_tags",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var descriptions: MutableList<ProductDescription> = mutableListOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val gallery: MutableList<ProductGallery> = mutableListOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variants: MutableList<ProductVariant> = mutableListOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val seoContent: MutableList<ProductSeoContent> = mutableListOf()

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reviews: MutableList<Review> = mutableListOf()

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "product_occasions",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "occasion_id")]
    )
    val occasions: MutableSet<Occasion> = mutableSetOf()

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @LastModifiedDate
    val updatedAt: Instant = Instant.now()
}
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
        Index(name = "idx_products_featured", columnList = "featured, status")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Product(
    var title: String = "",
    var slug: String = "",
    var status: Boolean = true,
    var price: BigDecimal = BigDecimal.ZERO,
    var stockStatus: StockStatus = StockStatus.AVAILABLE,
    var seasonal: Boolean = false,
    var featured: Boolean = false,
    var facebookUrl: String? = null,
    var instagramUrl: String? = null,
    var views: Int = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    lateinit var category: Category

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    lateinit var subCategory: SubCategory

    @ManyToMany
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
    val reviews: MutableList<Review> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()

    fun getFullPath(): String {
        return "/productos/${category.route}/${subCategory.route}/$slug"
    }
}

enum class StockStatus {
    AVAILABLE,
    SEASONAL,
    OUT_OF_STOCK
}
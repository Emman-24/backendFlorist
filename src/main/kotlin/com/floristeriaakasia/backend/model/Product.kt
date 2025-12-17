package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products")
class Product(
    var title: String = "",
    var route: String = "",
    var status: Boolean = true,
    var price: BigDecimal = BigDecimal.ZERO,
    var originalName: String = "",
    var storedName: String = "",
    var mimeType: String = "",
    var size: Long = Long.MIN_VALUE,
    var facebookUrl: String = "",
    var instagramUrl: String = "",
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
    var tags: MutableList<Tag> = mutableListOf()

    @ElementCollection
    @CollectionTable(name = "product_descriptions", joinColumns = [JoinColumn(name = "product_id")])
    @OrderColumn(name = "position")
    @Column(name = "paragraph", columnDefinition = "TEXT")
    var description: MutableList<String>? = null

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @CreationTimestamp
    val updatedAt: Instant? = null
}
package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant

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

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "product_tags",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableList<Tag> = mutableListOf(),

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

    @Column(
        name = "original_name",
        length = 1024
    )
    var originalName: String,

    @Column(
        name = "stored_name",
        length = 1024
    )
    var storedName: String,

    @Column(
        name = "mime_type",
        length = 1024
    )
    var mimeType: String,

    @Column(length = 1024)
    var size: Long,

    @Column(
        name = "facebook_url",
        length = 1024
    )
    var facebookUrl: String? = null,

    @Column(
        name = "instagram_url",
        length = 1024
    )
    var instagramUrl: String? = null,

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()
)
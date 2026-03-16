package com.floristeriaakasia.backend.feature.floralArrangment.domain

import com.floristeriaakasia.backend.feature.category.Category
import com.floristeriaakasia.backend.feature.flowers.Flowers
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductGallery
import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import com.floristeriaakasia.backend.feature.tag.Tag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

@Entity
@Table(name = "floral_arrangements")
data class FloralArrangement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var seoName: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "arrangement_categories",
        joinColumns = [JoinColumn(name = "arrangement_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    var categories: MutableSet<Category> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var price: Price,

    @Column(name = "is_available", nullable = false)
    var isAvailable: Boolean,

    @Column(name = "seasonal", nullable = false)
    var seasonal: Boolean,

    @Column(name = "featured", nullable = false)
    var featured: Boolean,

    @Column(name = "views", nullable = false)
    var views: Int = 0,

    @Version
    var version: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now(),

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "description_id")
    var description: ProductDescription? = null,

    @OneToMany(mappedBy = "floralArrangement", cascade = [CascadeType.ALL], orphanRemoval = true)
    var flowers: MutableSet<Flowers> = mutableSetOf(),

    @OneToMany(mappedBy = "floralArrangement", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("position ASC")
    val gallery: MutableSet<ProductGallery> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "arrangement_tags",
        joinColumns = [JoinColumn(name = "arrangement_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf()

)
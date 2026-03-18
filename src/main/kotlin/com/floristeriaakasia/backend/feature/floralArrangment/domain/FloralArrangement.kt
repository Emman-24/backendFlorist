package com.floristeriaakasia.backend.feature.floralArrangment.domain

import com.floristeriaakasia.backend.feature.category.Category
import com.floristeriaakasia.backend.feature.flowers.Flowers
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import com.floristeriaakasia.backend.feature.tag.Tag
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

@Entity
@Table(name = "floral_arrangements")
class FloralArrangement(
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

){
    fun addImage(image: ProductGallery) {
        if (gallery.isEmpty()) {
            image.isPrimary = true
        }
        gallery.add(image)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloralArrangement) return false
        if (id != 0L && other.id != 0L) return id == other.id
        return slug == other.slug
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else slug.hashCode()
    }

    override fun toString(): String {
        return "FloralArrangement(id=$id, name='$name', slug='$slug')"
    }
}
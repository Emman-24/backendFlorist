package com.floristeriaakasia.backend.feature.product.adapter.out.persistence

import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import com.floristeriaakasia.backend.feature.tag.Tag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "floral_arrangements")
@EntityListeners(AuditingEntityListener::class)
data class FloralArrangement(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long,

    @Column(name = "price_amount", nullable = false)
    val priceAmount: BigDecimal,

    @Column(name = "discount_price_amount")
    val discountPriceAmount: BigDecimal? = null,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String = "COP",

    @Column(name = "stock_status", nullable = false)
    var stockStatus: String,

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
    @OrderBy("position ASC")
    val gallery: MutableList<ProductGallery> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "arrangement_tags",
        joinColumns = [JoinColumn(name = "arrangement_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf()

){
    fun addGalleryEntity(imageEntity: ProductGallery) {
        gallery.add(imageEntity)
        imageEntity.floralArrangement = this
    }
}
package com.floristeriaakasia.backend.feature.floralArrangment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "product_gallery")
class ProductGallery(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var publicId: String = "",

    @Column(nullable = false)
    var originalUrl: String = "",

    @Column(nullable = false)
    var thumbnailUrl: String = "",

    @Column(nullable = false)
    var mediumUrl: String = "",

    @Column(nullable = false)
    var altText: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floralArrangement_id", nullable = false)
    var floralArrangement: FloralArrangement? = null,

    @Column(nullable = false)
    var isPrimary: Boolean = false,

    @Column(nullable = false)
    var position: Int = 0,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductGallery) return false
        if (id != 0L && other.id != 0L) return id == other.id
        return publicId == other.publicId
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else publicId.hashCode()
    }

    override fun toString(): String {
        return "ProductGallery(id=$id, publicId='$publicId', isPrimary=$isPrimary, position=$position)"
    }
}
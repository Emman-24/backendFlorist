package com.floristeriaakasia.backend.feature.product.adapter.out.persistence

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
data class ProductGallery(

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

)
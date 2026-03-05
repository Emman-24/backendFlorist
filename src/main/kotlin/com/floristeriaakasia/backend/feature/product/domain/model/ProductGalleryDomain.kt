package com.floristeriaakasia.backend.feature.product.domain.model

data class ProductGalleryDomain(
    val id: Long? = null,
    val publicId: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val mediumUrl: String,
    var altText: String,
    var isPrimary: Boolean = false,
    var position: Int = 0
)

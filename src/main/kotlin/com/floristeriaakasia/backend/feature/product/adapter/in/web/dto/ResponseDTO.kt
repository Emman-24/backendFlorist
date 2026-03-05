package com.floristeriaakasia.backend.feature.product.adapter.`in`.web.dto

import java.math.BigDecimal

data class ProductResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val categoryId: Long,
    val priceAmount: BigDecimal,
    val discountPriceAmount: BigDecimal?,
    val currency: String,
    val stockStatus: String,
    val seasonal: Boolean,
    val featured: Boolean,
    val facebookUrl: String?,
    val instagramUrl: String?,
    val gallery: List<GalleryImageResponse>
)

data class GalleryImageResponse(
    val publicId: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val mediumUrl: String,
    val altText: String,
    val isPrimary: Boolean,
    val position: Int
)
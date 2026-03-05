package com.floristeriaakasia.backend.feature.product.adapter.`in`.web.mapper

import com.floristeriaakasia.backend.feature.product.adapter.`in`.web.dto.GalleryImageResponse
import com.floristeriaakasia.backend.feature.product.adapter.`in`.web.dto.ProductResponse
import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain
import com.floristeriaakasia.backend.feature.product.domain.model.ProductGalleryDomain

fun FloralArrangementDomain.toResponse(): ProductResponse {
    return ProductResponse(
        id = this.id ?: throw IllegalStateException("Cannot map an unpersisted product to a web response"),
        name = this.name,
        slug = this.slug,
        categoryId = this.categoryId,

        // Flattening the Money Value Object for the JSON response
        priceAmount = this.price.amount,
        discountPriceAmount = this.discountPrice?.amount,
        currency = this.price.currency,

        stockStatus = this.stockStatus.name,
        seasonal = this.seasonal,
        featured = this.featured,

        // Assuming you add these to your ProductDescriptionDomain or metadata later
        facebookUrl = null,
        instagramUrl = null,

        // Map the nested gallery list safely
        gallery = this.gallery.map { it.toResponse() }.sortedBy { it.position }
    )
}

fun ProductGalleryDomain.toResponse(): GalleryImageResponse {
    return GalleryImageResponse(
        publicId = this.publicId,
        originalUrl = this.originalUrl,
        thumbnailUrl = this.thumbnailUrl,
        mediumUrl = this.mediumUrl,
        altText = this.altText,
        isPrimary = this.isPrimary,
        position = this.position
    )
}
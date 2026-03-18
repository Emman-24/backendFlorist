package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import java.math.BigDecimal
import java.util.Collections.emptySet


data class CreateFloralArrangementRequest(
    val name: String,
    val seoName: String,
    val categoryIds: Set<Long>,
    val price: BigDecimal,
    val discountPrice: BigDecimal? = null,
    val currency: String,
    val isAvailable: Boolean,
    val seasonal: Boolean,
    val featured: Boolean,
    val shortDescription: String,
    val description: String,

    val flowers: MutableSet<FlowerRequest> = emptySet(),
    val tagIds: Set<Long> = emptySet()
) {
    data class FlowerRequest(
        val name: String,
        val meaning: String
    )
}

data class FloralArrangementDetailDto(
    val id: Long,
    val name: String,
    val slug: String,
    val seoName: String,
    val price: PriceSummaryDto,
    val isAvailable: Boolean,
    val seasonal: Boolean,
    val featured: Boolean,
    val views: Int,
    val description: DescriptionDto?,
    val gallery: List<ImageDto>,
    val flowers: List<FlowerDto>,
    val categories: List<CategorySummaryDto>,
    val tags: List<TagSummaryDto>,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)

data class FloralArrangementQuery(
    val categoryId: Long? = null,
    val tagId: Long? = null,
    val featured: Boolean? = null,
    val seasonal: Boolean? = null,
    val isAvailable: Boolean? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null
)

data class FloralArrangementSummaryDto(
    val id: Long,
    val name: String,
    val slug: String,
    val seoName: String,
    val price: PriceSummaryDto,
    val isAvailable: Boolean,
    val seasonal: Boolean,
    val featured: Boolean,
    val primaryImage: ImageDto?,
    val categories: List<CategorySummaryDto>,
    val tags: List<TagSummaryDto>
)

data class PriceSummaryDto(
    val amount: BigDecimal,
    val discountAmount: BigDecimal?,
    val currency: String,
    val hasDiscount: Boolean,
    val discountPercent: Int?
)

data class ImageDto(
    val publicId: String,
    val originalUrl: String,
    val thumbnailUrl: String,
    val mediumUrl: String,
    val altText: String,
    val isPrimary: Boolean,
    val position: Int
)

data class DescriptionDto(
    val shortDescription: String,
    val description: String
)

data class FlowerDto(
    val id: Long?,
    val name: String,
    val meaning: String
)

data class CategorySummaryDto(
    val id: Long,
    val name: String,
    val slug: String,
    val path: String
)

data class TagSummaryDto(
    val id: Long,
    val text: String,
    val route: String
)

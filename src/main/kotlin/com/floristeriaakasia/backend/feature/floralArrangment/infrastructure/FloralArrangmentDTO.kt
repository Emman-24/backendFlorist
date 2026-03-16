package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import java.math.BigDecimal
import java.util.Collections.emptySet


data class CreateFloralArrangementRequest(
    val name: String,
    val seoName: String,
    val slug: String,
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
package com.floristeriaakasia.backend.feature.floralArrangment.application

import java.math.BigDecimal
import java.util.Collections

data class CreateFloralArrangementCommand(
    val name: String,
    val seoName: String,
    val categoryIds: Set<Long>,
    val priceAmount: BigDecimal,
    val discountPriceAmount: BigDecimal? = null,
    val currency: String = "COP",
    val isAvailable: Boolean,
    val seasonal: Boolean,
    val featured: Boolean,
    val description: String,

    // ── Optional nested data ──────────────────────────────────────────────
    val shortDescription: String,
    val flowers: List<FlowerData> = emptyList(),
    val tagIds: Set<Long> = Collections.emptySet()
){
    init {
        require(name.isNotBlank()) { "Arrangement name must not be blank" }
        require(categoryIds.isNotEmpty()) { "At least one category must be provided" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be greater than zero" }
        require(currency.length == 3) { "Currency must be an ISO-4217 code (e.g. COP, USD)" }
        discountPriceAmount?.let {
            require(it < priceAmount) { "Discount price must be less than the original price" }
        }
        require(shortDescription.isNotBlank()) { "shortDescription is required when description is provided" }
        require(description.isNotBlank()) { "description is required when shortDescription is provided" }
    }


    data class FlowerData(
        val name: String,
        val meaning: String
    ) {
        init {
            require(name.isNotBlank()) { "Flower name must not be blank" }
            require(meaning.isNotBlank()) { "Flower meaning must not be blank" }
        }
    }
}
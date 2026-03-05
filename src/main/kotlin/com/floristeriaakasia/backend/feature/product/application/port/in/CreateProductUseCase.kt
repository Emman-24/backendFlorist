package com.floristeriaakasia.backend.feature.product.application.port.`in`

import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain
import java.math.BigDecimal

interface CreateProductUseCase {
    fun execute(command: CreateProductCommand): FloralArrangementDomain
}

data class CreateProductCommand(
    val name: String,
    val slug: String,
    val categoryId: Long, val priceAmount: BigDecimal,
    val discountPrice: BigDecimal? = null,
    val currency: String = "COP",
    val seasonal: Boolean = false,
    val featured: Boolean = false,
    val facebookUrl: String? = null,
    val instagramUrl: String? = null,
)
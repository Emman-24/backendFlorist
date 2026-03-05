package com.floristeriaakasia.backend.feature.product.adapter.`in`.web.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import java.math.BigDecimal

data class CreateProductRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\s\\u00C0-\\u017F,.'\"()\\-&]+$",
        message = "Name contains invalid characters"
    )
    val name: String,

    @field:NotBlank(message = "Slug is required")
    @field:Size(min = 3, max = 200)
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers and hyphens")
    val slug: String,

    @field:NotNull
    @field:Positive
    var categoryId: Long,

    @field:NotNull(message = "Price is required")
    @field:DecimalMin("0.01")
    var priceAmount: BigDecimal,

    @field:DecimalMin("0.01")
    val discountPriceAmount: BigDecimal? = null,

    @field:NotBlank(message = "Currency code is required")
    @field:Size(min = 3, max = 3)
    val currency: String = "COP",

    @field:NotBlank(message = "Stock status is required")
    val stockStatus: String = "AVAILABLE",

    val seasonal: Boolean = false,
    val featured: Boolean = false,

    @field:URL(message = "Invalid Facebook URL")
    @field:Size(max = 500)
    val facebookUrl: String? = null,

    @field:URL(message = "Invalid Instagram URL")
    @field:Size(max = 500)
    val instagramUrl: String? = null
)
package com.floristeriaakasia.backend.model.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

data class ProductCreateRequest(

    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    val title: String = "",


    @field:NotBlank(message = "Route is required")
    @field:Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Route must contain only lowercase letters, numbers and dashes"
    )
    val route: String = "",


    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    var price: BigDecimal = BigDecimal.ZERO,


    @field:NotNull(message = "Category is required")
    @field:Min(value = 1, message = "Category id must be greater than zero")
    var categoryId: Long? = null,


    @field:NotNull(message = "Subcategory is required")
    @field:Min(value = 1, message = "Subcategory id must be greater than zero")
    var subcategoryId: Long? = null,


    @field:NotBlank(message = "The stock status is required")
    val stockStatus: String = "available",


    val seasonal: Boolean? = false,


    val featured: Boolean? = false,


    @field:Size(max = 500, message = "The facebook url must be less than 500 characters")
    val facebookUrl: String? = null,


    @field:Size(max = 500, message = "The instagram url must be less than 500 characters")
    val instagramUrl: String? = null,


    val status: Boolean? = true,


    val tagIds: List<Long> = emptyList()
)
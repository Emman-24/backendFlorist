package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.StockStatus
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.URL
import java.math.BigDecimal

data class ProductCreateRequest(

    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\s\\u00C0-\\u017F,.'\"()\\-&]+$",
        message = "Title contains invalid characters"
    )
    val title: String,


    @field:NotBlank(message = "Slug is required")
    @field:Size(min = 3, max = 200, message = "Slug must be between 3 and 200 characters")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers and hyphens")
    val slug: String,


    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @field:DecimalMax(value = "999999.99", message = "Price is too high")
    var price: BigDecimal,


    @field:NotNull(message = "Category is required")
    @field:Min(1)
    var categoryId: Long,


    @field:NotNull(message = "Subcategory is required")
    @field:Min(1)
    var subcategoryId: Long,


    val stockStatus: StockStatus = StockStatus.AVAILABLE,
    val seasonal: Boolean = false,
    val featured: Boolean = false,

    @field:URL(message = "Invalid Facebook URL")
    @field:Size(max = 500, message = "Facebook URL is too long")
    val facebookUrl: String? = null,

    @field:URL(message = "Invalid Instagram URL")
    @field:Size(max = 500, message = "Instagram URL is too long")
    val instagramUrl: String? = null,

    val status: Boolean = true,
)
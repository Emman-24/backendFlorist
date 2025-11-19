package com.floristeriaakasia.backend.model.dto.product

import jakarta.validation.constraints.*
import java.math.BigDecimal

class ProductCreateRequest(

    @field:NotNull
    @field:Min(1)
    var categoryId: Long,

    @field:NotNull
    @field:Min(1)
    var subCategoryId: Long,

    @field:NotBlank(message = "Product route is required")
    @field:Size(min = 3, max = 50, message = "Route must be between 3 and 50 characters")
    val route: String,

    val status: Boolean? = true,

    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val text: String,

    @field:NotBlank(message = "Product description is required")
    var description: String,

    @field:NotNull(message = "Product price is required")
    @field:DecimalMin(value = "0.00", inclusive = true, message = "Price must be at least 0.00")
    var price: BigDecimal?,

    val facebookUrl: String? = null,

    val instagramUrl: String? = null,
)
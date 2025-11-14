package com.floristeriaakasia.backend.model.dto.subcategory

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SubCategoryCreateRequest(
    @field:NotBlank(message = "SubCategory name is required")
    @field:Size(min = 3, max = 50)
    val text: String,

    @field:NotBlank(message = "SubCategory route is required")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Route must be lowercase, numbers, and hyphens only")
    val route: String,

    val status: Boolean,

    @field:NotNull
    @field:Min(1)
    val categoryId: Long
)

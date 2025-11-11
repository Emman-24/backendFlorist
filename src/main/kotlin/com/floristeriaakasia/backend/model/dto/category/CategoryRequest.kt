package com.floristeriaakasia.backend.model.dto.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class CategoryRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    val text: String,

    @field:NotBlank(message = "Category route is required")
    @field:Size(min = 3, max = 50, message = "Route must be between 3 and 50 characters")
    @field:Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Route must be lowercase, numbers, and hyphens only"
    )
    val route: String,

    val status: Boolean
)
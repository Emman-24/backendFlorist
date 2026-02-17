package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CategoryCreateRequest(
    @field:NotBlank(message = "Category name is required")
    @field:Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\s\\u00C0-\\u017F,.'\"()\\-&]+$",
        message = "Category name contains invalid characters"
    )
    val text: String,

    @field:NotBlank(message = "Route is required")
    @field:Size(min = 2, max = 100, message = "Route must be between 2 and 100 characters")
    @field:Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "Route must contain only lowercase letters, numbers and hyphens"
    )
    val route: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 500, message = "Description must be less than 500 characters")
    val description: String,

    val position: Int = 0,

    val status: Boolean = true
) {
    companion object {
        fun toCategory(dto: CategoryCreateRequest): Category {
            return Category(
                text = dto.text,
                route = dto.route,
                description = dto.description,
                position = dto.position,
                status = dto.status
            )

        }
    }

}

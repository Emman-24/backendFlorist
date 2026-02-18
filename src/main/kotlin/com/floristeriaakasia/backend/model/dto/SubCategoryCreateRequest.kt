package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.SubCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SubCategoryCreateRequest(

    @field:NotBlank(message = "Subcategory name is required")
    @field:Size(min = 2, max = 100, message = "Subcategory name must be between 2 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\s\\u00C0-\\u017F,.'\"()\\-&]+$",
        message = "Subcategory name contains invalid characters"
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
    val status: Boolean = true,
    val categoryId: Long
) {
    companion object {
        fun toSubCategory(dto: SubCategoryCreateRequest): SubCategory {
            return SubCategory(
                text = dto.text,
                route = dto.route,
                description = dto.description,
                position = dto.position,
                status = dto.status
            )
        }
    }
}

data class SubCategoryResponse(
    val id: Long,
    val text: String,
    val route: String,
    val description: String,
    val position: Int,
    val status: Boolean,
    val category: CategorySimpleResponse
) {
    companion object {
        fun from(subCategory: SubCategory,category: Category) = SubCategoryResponse(
            id = subCategory.id!!,
            text = subCategory.text,
            route = subCategory.route,
            description = subCategory.description,
            position = subCategory.position,
            status = subCategory.status,
            category = CategorySimpleResponse.from(category)
        )
    }
}

data class SubcategorySimpleResponse(
    val id: Long,
    val text: String,
    val route: String,
    val description: String,
    val position: Int,
    val status: Boolean
){
    companion object {
        fun from(subcategory: SubCategory) = SubcategorySimpleResponse(
            id = subcategory.id!!,
            text = subcategory.text,
            route = subcategory.route,
            description = subcategory.description,
            position = subcategory.position,
            status = subcategory.status
        )
    }
}

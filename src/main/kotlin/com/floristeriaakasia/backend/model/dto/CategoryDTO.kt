package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.Category

data class CategoryDTO(
    val name: String,
    val route: String,
    val description: String?,
    val subCategories: List<SubCategorySimpleDTO>
) {
    companion object {
        fun from(category: Category): CategoryDTO {
            return CategoryDTO(
                name = category.text,
                route = category.route,
                description = null,
                subCategories = category.subCategories.map { subCategory ->
                    SubCategorySimpleDTO(
                        id = subCategory.id!!,
                        name = subCategory.text,
                        route = subCategory.route
                    )
                }
            )
        }
    }
}

data class CategorySimpleResponse(
    val name: String,
    val route: String,
    val description: String
){
    companion object {
        fun from(category: Category) = CategorySimpleResponse(
            name = category.text,
            route = category.route,
            description = category.description
        )
    }
}
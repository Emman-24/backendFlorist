package com.floristeriaakasia.backend.model.dto.category

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.dto.subcategory.SubcategoryMapper
import org.springframework.stereotype.Component

@Component
class CategoryMapper(
    val subcategoryMapper: SubcategoryMapper
) {
    fun toResponse(category: Category): CategoryResponse {
        return CategoryResponse(
            id = category.id!!,
            text = category.text,
            route = category.route,
            status = category.status,
            subCategories = category.subCategories.map(subcategoryMapper::toResponse),
            createdAt = category.createdAt
        )
    }

    fun toEntity(request: CategoryRequest): Category {
        return Category(
            text = request.text,
            route = request.route,
            status = request.status
        )
    }

    fun updateEntityFromRequest(request: CategoryRequest, category: Category) {
        category.text = request.text
        category.route = request.route
        category.status = request.status
    }
}
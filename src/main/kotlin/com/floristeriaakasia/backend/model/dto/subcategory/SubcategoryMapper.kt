package com.floristeriaakasia.backend.model.dto.subcategory

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.stereotype.Component

@Component
class SubcategoryMapper {
    fun toResponse(subCategory: SubCategory): SubcategoryResponse {
        return SubcategoryResponse(
            id = subCategory.id!!,
            text = subCategory.text,
            route = subCategory.route,
            status = subCategory.status,
            categoryName = subCategory.category.text,
            createdAt = subCategory.createdAt.toString()
        )
    }

    fun toEntity(request: SubCategoryCreateRequest, parentCategory: Category): SubCategory {
        return SubCategory(
            text = request.text,
            route = request.route,
            status = request.status,
            category = parentCategory
        )
    }

    fun toRequest(subCategory: SubCategory): SubcategoryRequest {
        return SubcategoryRequest(
            text = subCategory.text,
            route = subCategory.route,
            status = subCategory.status
        )
    }

    fun toCreateRequest(subCategory: SubCategory): SubCategoryCreateRequest {
        return SubCategoryCreateRequest(
            text = subCategory.text,
            route = subCategory.route,
            status = subCategory.status,
            categoryId = subCategory.category.id!!
        )
    }

    fun updateEntityFromRequest(request: SubcategoryRequest, subCategory: SubCategory) {
        subCategory.text = request.text
        subCategory.route = request.route
        subCategory.status = request.status
    }
}
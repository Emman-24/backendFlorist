package com.floristeriaakasia.backend.model.dto.category

import com.floristeriaakasia.backend.model.dto.subcategory.SubcategoryResponse
import java.time.Instant

data class CategoryResponse(
    val id: Long,
    val text: String,
    val route: String,
    val status: Boolean,
    val subCategories: List<SubcategoryResponse>,
    val createdAt: Instant
)

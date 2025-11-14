package com.floristeriaakasia.backend.model.dto.subcategory

data class SubcategoryResponse(
    val id: Long,
    val text: String,
    val route: String,
    val categoryName: String,
    val status: Boolean,
    val createdAt: String
)
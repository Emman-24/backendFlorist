package com.floristeriaakasia.backend.model.dto.product

data class ProductResponse(
    val id: Long,
    val text: String,
    val categoryName: String,
    val subcategoryName: String,
    val price: Int,
    val imageUrl: String,
    val status: Boolean,
    val createdAt: String,
)

package com.floristeriaakasia.backend.model.dto.category

import java.time.Instant

data class CategoryResponse(
    val id: Long,
    val text: String,
    val route: String,
    val status: Boolean,
    val createdAt: Instant
)

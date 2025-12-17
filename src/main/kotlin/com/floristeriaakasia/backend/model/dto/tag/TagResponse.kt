package com.floristeriaakasia.backend.model.dto.tag

import com.floristeriaakasia.backend.model.dto.product.ProductResponse
import java.time.Instant

data class TagResponse(
    val id: Long,
    val text: String,
    val route: String,
    val status: Boolean,
    val products: List<ProductResponse>,
    val createdAt: Instant
)

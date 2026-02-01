package com.floristeriaakasia.backend.model.dto

import java.math.BigDecimal

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: MutableList<String>,
    val price: BigDecimal,
    val stock: Int,
    val imageUrl: String?,
    val isActive: Boolean,
    val available: Boolean
)

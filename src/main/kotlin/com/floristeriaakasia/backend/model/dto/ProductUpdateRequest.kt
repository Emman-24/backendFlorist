package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.StockStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

import java.math.BigDecimal

data class ProductUpdateRequest(

    @field:NotBlank(message = "El título es obligatorio")
    @field:Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    val title: String,

    @field:NotBlank
    @field:Pattern(regexp = "^[a-z0-9-]+$")
    val slug: String,

    @field:NotNull
    @field:DecimalMin("0.01")
    var price: BigDecimal,

    @field:NotNull(message = "La categoría es obligatoria")
    var categoryId: Long,

    @field:NotNull(message = "La subcategoría es obligatoria")
    var subcategoryId: Long,

    var stockStatus: StockStatus,
    val seasonal: Boolean,
    val featured: Boolean,
    val facebookUrl: String?,
    val instagramUrl: String?,
    val status: Boolean
)

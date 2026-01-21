package com.floristeriaakasia.backend.model.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

import java.math.BigDecimal

data class ProductUpdateRequest(
    @field:NotBlank(message = "El título es obligatorio")
    @field:Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    val title: String = "",

    @field:NotBlank(message = "La ruta es obligatoria")
    @field:Pattern(
        regexp = "^[a-z0-9-]+$",
        message = "La ruta solo puede contener letras minúsculas, números y guiones"
    )
    val route: String = "",

    @field:NotNull(message = "El precio es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    var price: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "La categoría es obligatoria")
    var categoryId: Long? = null,

    @field:NotNull(message = "La subcategoría es obligatoria")
    var subcategoryId: Long? = null,

    @field:NotNull(message = "El estado de stock es obligatorio")
    var stockStatus: String = "available",

    val seasonal: Boolean? = false,

    val featured: Boolean? = false,

    @field:Size(max = 500, message = "La URL de Facebook no puede exceder 500 caracteres")
    val facebookUrl: String? = null,

    @field:Size(max = 500, message = "La URL de Instagram no puede exceder 500 caracteres")
    val instagramUrl: String? = null,

    val status: Boolean? = true,

    val tagIds: List<Long> = emptyList(),

    /**
     * Flag para indicar si se debe regenerar SEO metadata
     * false = mantener metadata personalizada
     * true = regenerar automáticamente
     */
    val updateSeo: Boolean = false
)

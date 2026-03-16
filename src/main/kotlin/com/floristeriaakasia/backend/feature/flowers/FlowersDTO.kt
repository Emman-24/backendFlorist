package com.floristeriaakasia.backend.feature.flowers

import jakarta.validation.constraints.NotBlank

data class CreateFlowersDTO(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotBlank(message = "meaning of the flower is required")
    val meaning: String,

    @field:NotBlank(message = "floral arrangement id is required")
    val floralArrangementId: Long
)
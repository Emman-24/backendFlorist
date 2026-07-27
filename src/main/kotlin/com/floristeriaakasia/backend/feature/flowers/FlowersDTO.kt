package com.floristeriaakasia.backend.feature.flowers

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CreateFlowersDTO(
    @field:NotBlank(message = "name is required")
    val name: String,

    @field:NotBlank(message = "meaning of the flower is required")
    val meaning: String,

    @field:NotNull(message = "floral arrangement id is required")
    @field:Positive(message = "floral arrangement id must be positive")
    var floralArrangementId: Long
)

data class UpdateFlowersDTO(
    @field:NotBlank(message = "name is required")
    val name: String,
    @field:NotBlank(message = "meaning of the flower is required")
    val meaning: String
)

data class FlowerResponse(
    val id: Long,
    val name: String,
    val meaning: String,
    val floralArrangementId: Long?
) {
    companion object {
        fun from(f: Flowers) = FlowerResponse(
            id = f.id!!,
            name = f.name,
            meaning = f.meaning,
            floralArrangementId = f.floralArrangement?.id
        )
    }
}
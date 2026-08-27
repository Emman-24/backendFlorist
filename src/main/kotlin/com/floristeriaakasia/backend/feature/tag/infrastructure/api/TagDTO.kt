package com.floristeriaakasia.backend.feature.tag.infrastructure.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateTagRequest(
    @field:NotBlank(message = "Tag text is required")
    @field:Size(min = 2, max = 50, message = "Tag text must be between 2 and 50 characters")
    val text: String,

    @field:NotBlank(message = "Route is required")
    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "Route must contain only lowercase letters, numbers, and hyphens")
    @field:Size(min = 2, max = 50, message = "Route must be between 2 and 50 characters")
    val route: String,

    val status: Boolean = true
)

data class TagResponse(
    val id: Long,
    val text: String,
    val route: String,
    val status: Boolean
) {
    companion object {
        fun from(tag: Tag): TagResponse {
            return TagResponse(
                id = tag.id!!,
                text = tag.text,
                route = tag.route,
                status = tag.status
            )
        }
    }
}


data class TagDTO(
    val id: Long,
    val name: String,
    val route: String
) {
    companion object {
        fun from(tag: Tag) = TagDTO(
            id = tag.id!!,
            name = tag.text,
            route = tag.route
        )
    }
}
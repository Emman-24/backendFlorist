package com.floristeriaakasia.backend.feature.faq

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateFaqRequest(
    @field:NotBlank(message = "Question is required")
    @field:Size(min = 10, max = 255, message = "Question must be between 10 and 255 characters")
    val question: String,

    @field:NotBlank(message = "Answer is required")
    @field:Size(min = 10, max = 2000, message = "Answer must be between 10 and 2000 characters")
    val answer: String,

    @field:Min(value = 0, message = "Position must be 0 or greater")
    @field:Max(value = 9999, message = "Position must be less than 10000")
    val position: Int = 0,

    val status: Boolean = true
)

data class UpdateFaqRequest(
    @field:NotBlank(message = "Question is required")
    @field:Size(min = 10, max = 255, message = "Question must be between 10 and 255 characters")
    val question: String,

    @field:NotBlank(message = "Answer is required")
    @field:Size(min = 10, max = 2000, message = "Answer must be between 10 and 2000 characters")
    val answer: String,

    @field:Min(value = 0, message = "Position must be 0 or greater")
    val position: Int = 0,

    val status: Boolean = true
)

data class UpdateFaqStatusRequest(
    val status: Boolean
)


data class FaqResponse(
    val id: Long,
    val question: String,
    val answer: String,
    val position: Int,
    val views: Int,
    val status: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(faq: Faq) = FaqResponse(
            id = faq.id,
            question = faq.question,
            answer = faq.answer,
            position = faq.position,
            views = faq.views,
            status = faq.status,
            createdAt = faq.createdAt,
            updatedAt = faq.updatedAt
        )
    }
}
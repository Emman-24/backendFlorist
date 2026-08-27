package com.floristeriaakasia.backend.feature.faq.infrastructure.api

import com.floristeriaakasia.backend.util.ApiResponse
import com.floristeriaakasia.backend.util.toSuccessResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/faqs")
class FaqController(
    private val faqService: FaqService
) {

    @GetMapping
    fun getAllActive(): ResponseEntity<ApiResponse<List<FaqResponse>>> {
        val faqs = faqService.findAll(onlyActive = true)
        return ResponseEntity.ok(faqs.toSuccessResponse())
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<FaqResponse>> {
        val faq = faqService.findByIdAndIncrementViews(id)
        return ResponseEntity.ok(faq.toSuccessResponse())
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAll(): ResponseEntity<ApiResponse<List<FaqResponse>>> {
        val faqs = faqService.findAll(onlyActive = false)
        return ResponseEntity.ok(faqs.toSuccessResponse())
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun create(
        @Valid @RequestBody request: CreateFaqRequest
    ): ResponseEntity<ApiResponse<FaqResponse>> {
        val created = faqService.create(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created.toSuccessResponse("FAQ created successfully"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateFaqRequest
    ): ResponseEntity<ApiResponse<FaqResponse>> {
        val updated = faqService.update(id, request)
        return ResponseEntity.ok(updated.toSuccessResponse("FAQ updated successfully"))
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun updateStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateFaqStatusRequest
    ): ResponseEntity<ApiResponse<FaqResponse>> {
        val updated = faqService.updateStatus(id, request.status)
        return ResponseEntity.ok(updated.toSuccessResponse())
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        faqService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun reorder(
        @RequestBody positions: Map<Long, Int>
    ): ResponseEntity<ApiResponse<Unit>> {
        faqService.reorder(positions)
        return ResponseEntity.ok(Unit.toSuccessResponse("FAQs reordered successfully"))
    }
}
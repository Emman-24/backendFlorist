package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.util.ApiResponse
import com.floristeriaakasia.backend.util.toSuccessResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createTag(
        @Valid @RequestBody request: CreateTagRequest
    ): ResponseEntity<ApiResponse<TagResponse>> {
        tagService.createTag(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping
    fun getAllTags(): ResponseEntity<ApiResponse<List<TagResponse>>> {
        val tags = tagService.getAllTags()
        val response = tags.map { TagResponse.from(it) }
        return ResponseEntity.ok(response.toSuccessResponse())
    }

    @PutMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun assignTagsToProduct(
        @PathVariable productId: Long,
        @RequestBody tagIds: List<Long>
    ): ResponseEntity<ApiResponse<Unit>> {
        tagService.assignTags(productId, tagIds)
        return ResponseEntity.ok(Unit.toSuccessResponse("Tags assigned to product successfully"))
    }

    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun removeTagsFromProduct(
        @PathVariable productId: Long,
        @RequestBody tagIds: List<Long>
    ): ResponseEntity<ApiResponse<Unit>> {
        tagService.removeTags(productId, tagIds)
        return ResponseEntity.ok(Unit.toSuccessResponse("Tags removed from product successfully"))
    }

    @GetMapping("/products/{productId}")
    fun getProductTags(
        @PathVariable productId: Long
    ): ResponseEntity<ApiResponse<List<TagResponse>>> {
        val tags = tagService.getProductTags(productId)
        val response = tags.map { TagResponse.from(it) }
        return ResponseEntity.ok(response.toSuccessResponse())
    }
}
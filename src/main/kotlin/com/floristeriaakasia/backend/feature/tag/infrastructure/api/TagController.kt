package com.floristeriaakasia.backend.feature.tag.infrastructure.api

import com.floristeriaakasia.backend.feature.tag.CreateTagRequest
import com.floristeriaakasia.backend.feature.tag.TagResponse
import com.floristeriaakasia.backend.feature.tag.TagService
import com.floristeriaakasia.backend.util.ApiResponse
import com.floristeriaakasia.backend.util.toSuccessResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tags")
class TagController(
    private val tagService: TagService
) {
    @PostMapping
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

    @GetMapping("/products/{productId}")
    fun getProductTags(
        @PathVariable productId: Long
    ): ResponseEntity<ApiResponse<List<TagResponse>>> {
        val tags = tagService.getProductTags(productId)
        val response = tags.map { TagResponse.from(it) }
        return ResponseEntity.ok(response.toSuccessResponse())
    }

    @PostMapping("/product/{productId}")
    fun assignTags(
        @PathVariable productId: Long,
        @RequestBody tagIds: List<Long>
    ): ResponseEntity<ApiResponse<Unit>> {
        tagService.assignTags(productId, tagIds)
        return ResponseEntity.ok().build()
    }
}
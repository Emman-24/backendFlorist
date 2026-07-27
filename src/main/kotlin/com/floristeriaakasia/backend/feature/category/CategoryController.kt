package com.floristeriaakasia.backend.feature.category

import com.floristeriaakasia.backend.util.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping("/root")
    @PreAuthorize("hasRole('ADMIN')")
    fun createRoot(
        @Valid @RequestBody request: CreateRootCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.createRoot(request.name, request.slug, request.displayOrder)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success(toResponse(category)))
    }

    @PostMapping("/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN')")
    fun createChild(
        @PathVariable parentId: Long,
        @Valid @RequestBody request: CreateChildCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.createChild(request.name, request.slug, parentId, request.displayOrder)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.Success(toResponse(category)))
    }

    @GetMapping("/tree")
    fun getFullTree(): ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> {
        val flat = categoryService.getFullTree()
        val tree = categoryService.buildTreeFromFlat(flat)
        return ResponseEntity.ok(ApiResponse.Success(tree.map(::toTreeResponse)))
    }

    @GetMapping
    fun getRoots(): ResponseEntity<ApiResponse<List<CategoryResponse>>> {
        val categories = categoryService.getRoots()
        return ResponseEntity.ok(ApiResponse.Success(categories.map { toResponse(it) }))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<CategoryResponse>> {
        val category = categoryService.getById(id)
        return ResponseEntity.ok(ApiResponse.Success(toResponse(category)))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> =
        ResponseEntity.ok(
            ApiResponse.Success(
                toResponse(
                    categoryService.update(
                        id,
                        request.name,
                        request.slug,
                        request.displayOrder,
                        request.description
                    )
                )
            )
        )

    @PatchMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    fun move(
        @PathVariable id: Long,
        @Valid @RequestBody request: MoveCategoryRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> =
        ResponseEntity.ok(ApiResponse.Success(toResponse(categoryService.move(id, request.newParentId))))

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateCategoryStatusRequest
    ): ResponseEntity<ApiResponse<CategoryResponse>> =
        ResponseEntity.ok(ApiResponse.Success(toResponse(categoryService.setStatus(id, request.status))))

}


private fun toResponse(c: Category) = CategoryResponse(
    id = c.id!!,
    name = c.name,
    slug = c.slug,
    path = c.path,
    parentId = c.parentId,
    depth = c.depth,
    displayOrder = c.displayOrder,
    description = c.description,
    isActive = c.status
)

private fun toTreeResponse(node: CategoryNode): CategoryTreeResponse = CategoryTreeResponse(
    category = toResponse(node.category),
    children = node.children.map(::toTreeResponse)
)
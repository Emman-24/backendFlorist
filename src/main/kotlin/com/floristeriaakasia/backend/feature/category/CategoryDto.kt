package com.floristeriaakasia.backend.feature.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateRootCategoryRequest(

    @field:NotBlank @field:Size(max = 150)
    val name: String,

    @field:NotBlank @field:Size(max = 180)
    @field:Pattern(regexp = "^[a-z0-9-]+$")
    val slug: String,

    val displayOrder: Int = 0

)

data class CreateChildCategoryRequest(

    @field:NotBlank @field:Size(max = 150) val name: String,

    @field:NotBlank @field:Size(max = 180)
    @field:Pattern(regexp = "^[a-z0-9-]+$") val slug: String,

    val displayOrder: Int = 0

)

data class CategoryTreeResponse(
    val category: CategoryResponse,
    val children: List<CategoryTreeResponse>
)

data class CategoryNode(
    val category: Category,
    val children: MutableList<CategoryNode> = mutableListOf()
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val path: String,
    val parentId: Long?,
    val depth: Int,
    val displayOrder: Int,
    val description: String?,
    val isActive: Boolean
)

data class MoveCategoryRequest(
    /** Null = move to root level */
    val newParentId: Long?
)

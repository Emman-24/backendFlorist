package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.dto.SubCategorySimpleDTO
import com.floristeriaakasia.backend.service.CategoryService
import com.floristeriaakasia.backend.service.CategoryStats
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(
        @RequestParam(required = false) active: Boolean?
    ): ResponseEntity<List<CategoryDTO>> {
        return try {
            val categories = if (active == true) {
                categoryService.findAllActive()
            } else {
                categoryService.findAll()
            }
            val dto = categories.map { CategoryDTO.from(it) }
            ResponseEntity.ok(dto)
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<Category> {
        return try {
            val category = categoryService.findById(id)
            if (category != null) {
                ResponseEntity.ok(category)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/route/{route}")
    fun getCategoryByRoute(@PathVariable route: String): ResponseEntity<Category> {
        return try {
            val category = categoryService.findByRoute(route)
            if (category != null) {
                ResponseEntity.ok(category)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}/stats")
    fun getCategoryStats(@PathVariable id: Long): ResponseEntity<CategoryStats> {
        return try {
            val stats = categoryService.getStats(id)
            if (stats != null) {
                ResponseEntity.ok(stats)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

}

data class CategoryDTO(
    val id: Long,
    val name: String,
    val route: String,
    val description: String?,
    val subCategories: List<SubCategorySimpleDTO>
) {
    companion object {
        fun from(category: Category): CategoryDTO {
            return CategoryDTO(
                id = category.id!!,
                name = category.text,
                route = category.route,
                description = null,
                subCategories = category.subCategories.map { subCategory ->
                    SubCategorySimpleDTO(
                        id = subCategory.id!!,
                        name = subCategory.text,
                        route = subCategory.route
                    )
                }
            )
        }
    }
}
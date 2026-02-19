package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.dto.CategoryCreateRequest
import com.floristeriaakasia.backend.model.dto.CategoryDTO
import com.floristeriaakasia.backend.service.CategoryService
import com.floristeriaakasia.backend.service.CategoryStats
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new category")
    fun createCategory(
        @Valid @RequestBody request: CategoryCreateRequest
    ): ResponseEntity<CategoryDTO> {
        return try {
            categoryService.save(CategoryCreateRequest.toCategory(request))
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

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
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<CategoryDTO> {
        return try {
            val category = categoryService.findById(id)
            if (category != null) {
                ResponseEntity.ok(CategoryDTO.from(category))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/route/{route}")
    fun getCategoryByRoute(@PathVariable route: String): ResponseEntity<CategoryDTO> {
        return try {
            val category = categoryService.findByRoute(route)
            if (category != null) {
                ResponseEntity.ok(CategoryDTO.from(category))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}/stats")
    fun getCategoryStats(
        @PathVariable id: Long
    ): ResponseEntity<CategoryStats> {
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


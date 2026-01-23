package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.service.CategoryService
import com.floristeriaakasia.backend.service.CategoryStats
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllCategories(@RequestParam(required = false) active: Boolean?): ResponseEntity<List<Category>> {
        return try {
            val categories = if (active == true) {
                categoryService.findAllActive()
            } else {
                categoryService.findAll()
            }
            ResponseEntity.ok(categories)
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

}
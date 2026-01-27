package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.service.SubcategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subcategories")
class SubcategoryController(
    private val subcategoryService: SubcategoryService
) {

    @GetMapping
    fun getAllSubcategories(): ResponseEntity<List<SubCategory>> {
        return try {
            val subcategories = subcategoryService.findAll()
            ResponseEntity.ok(subcategories)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @GetMapping("/{id}")
    fun getSubcategoryById(@PathVariable id: Long): ResponseEntity<SubCategory> {
        return try {
            val subcategory = subcategoryService.findById(id)
            if (subcategory != null) {
                ResponseEntity.ok(subcategory)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/route/{route}")
    fun getSubcategoryByRoute(@PathVariable route: String): ResponseEntity<SubCategory> {
        return try {
            val subcategory = subcategoryService.findByRoute(route)
            if (subcategory != null) {
                ResponseEntity.ok(subcategory)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/category/{categoryId}")
    fun getSubcategoriesByCategoryId(@PathVariable categoryId: Long): ResponseEntity<List<SubCategory>> {
        return try {
            val subcategories = subcategoryService.findByCategoryId(categoryId)
            ResponseEntity.ok(subcategories)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }
    
}

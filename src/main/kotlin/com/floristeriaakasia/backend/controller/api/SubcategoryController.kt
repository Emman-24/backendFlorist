package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.model.dto.SubCategoryCreateRequest
import com.floristeriaakasia.backend.model.dto.SubCategoryResponse
import com.floristeriaakasia.backend.model.dto.SubcategorySimpleResponse
import com.floristeriaakasia.backend.service.SubcategoryService

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subcategories")
class SubcategoryController(
    private val subcategoryService: SubcategoryService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createSubcategory(
        @Valid
        @RequestBody request: SubCategoryCreateRequest
    ): ResponseEntity<SubCategoryResponse> {
        return try {
            val subcategory = SubCategoryCreateRequest.toSubCategory(request)
            subcategoryService.save(subcategory, request.categoryId)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping
    fun getAllSubcategories(): ResponseEntity<List<SubCategoryResponse>> {
        return try {
            val subcategories = subcategoryService.findAll()
            ResponseEntity.ok(subcategories.map {
                SubCategoryResponse.from(it,it.category)
            })
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @GetMapping("/{id}")
    fun getSubcategoryById(@PathVariable id: Long): ResponseEntity<SubCategoryResponse> {
        return try {
            val subcategory = subcategoryService.findById(id)
            if (subcategory != null) {
                ResponseEntity.ok(SubCategoryResponse.from(subcategory,subcategory.category))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/route/{route}")
    fun getSubcategoryByRoute(@PathVariable route: String): ResponseEntity<SubCategoryResponse> {
        return try {
            val subcategory = subcategoryService.findByRoute(route)
            if (subcategory != null) {
                ResponseEntity.ok(SubCategoryResponse.from(subcategory,subcategory.category))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/category/{categoryId}")
    fun getSubcategoriesByCategoryId(@PathVariable categoryId: Long): ResponseEntity<List<SubcategorySimpleResponse>> {
        return try {
            val subcategories = subcategoryService.findByCategoryId(categoryId)
            ResponseEntity.ok(subcategories.map { SubcategorySimpleResponse.from(it) })
        } catch (_: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

}

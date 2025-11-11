package com.floristeriaakasia.backend.controller

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.service.CategoryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService
) {


}
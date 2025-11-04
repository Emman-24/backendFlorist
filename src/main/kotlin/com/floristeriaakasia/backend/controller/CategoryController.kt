package com.floristeriaakasia.backend.controller

import com.floristeriaakasia.backend.repository.CategoryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryRepository: CategoryRepository) {

    @GetMapping
    fun getAllCategories() = categoryRepository.findAll()

}
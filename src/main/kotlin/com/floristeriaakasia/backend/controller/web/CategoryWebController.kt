package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.dto.category.CategoryRequest
import com.floristeriaakasia.backend.service.CategoryService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/categories")
class CategoryWebController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun showCategoryList(model: Model): String {
        model.addAttribute("categories", categoryService.findAll())
        model.addAttribute("categoryRequest", CategoryRequest("", "", true))
        return "pages/categories/list"
    }

    @PostMapping
    fun createCategory(
        @Valid @ModelAttribute("categoryRequest") request: CategoryRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        try {
            categoryService.create(request)
        } catch (e: DataIntegrityViolationException) {
            bindingResult.rejectValue("text", "error.category", "Name or Route already exists.")
            model.addAttribute("categories", categoryService.findAll())
            return "pages/categories/list"
        }
        return "redirect:/categories"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute("categoryRequest", categoryService.findRequestById(id))
        model.addAttribute("categoryId", id)
        return "pages/categories/edit"
    }

    @PostMapping("/update/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @ModelAttribute("categoryRequest") request: CategoryRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        try {
            categoryService.update(id, request)
        } catch (e: DataIntegrityViolationException) {
            bindingResult.rejectValue("text", "error.category", "Name or Route already exists.")
            model.addAttribute("categoryId", id)
            return "pages/categories/edit"
        }
        return "redirect:/categories"
    }

    @PostMapping("/delete/{id}")
    fun deleteCategory(@PathVariable id: Long): String {
        categoryService.deleteById(id)
        return "redirect:/categories"
    }

}
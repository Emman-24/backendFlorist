package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.dto.subcategory.SubCategoryCreateRequest
import com.floristeriaakasia.backend.service.CategoryService
import com.floristeriaakasia.backend.service.SubcategoryService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/subcategories")
class SubcategoryWebController(
    private val subcategoryService: SubcategoryService,
    private val categoryService: CategoryService
) {

    @GetMapping
    fun showSubcategoryList(model: Model): String {
        model.addAttribute("categoriesForDropdown", categoryService.findAll())
        model.addAttribute("subcategories", subcategoryService.findAll())
        model.addAttribute("subCategoryCreateRequest", SubCategoryCreateRequest("", "", true, 0L))
        return "pages/subcategories/list"
    }

    @PostMapping
    fun createSubcategory(
        @Valid @ModelAttribute("subCategoryCreateRequest") request: SubCategoryCreateRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        try {
            subcategoryService.create(request)
        } catch (_: DataIntegrityViolationException) {
            bindingResult.rejectValue("text", "error.subcategory", "Name or Route already exists.")
            model.addAttribute("subCategoryCreateRequest", request)
            return "pages/subcategories/list"
        }
        return "redirect:/subcategories"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute("subCategoryCreateRequest", subcategoryService.findRequestById(id))
        /**
         * Category id is not an argument in subCategoryCreateRequest
         */
        model.addAttribute("categoryId")
        model.addAttribute("categoriesForDropdown", categoryService.findAll())
        return "pages/subcategories/edit"
    }

    @PostMapping("/delete/{id}")
    fun deleteSubcategory(
        @PathVariable id: Long
    ): String {
        subcategoryService.deleteById(id)
        return "redirect:/subcategories"
    }


}
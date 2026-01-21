package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.service.SubcategoryService
import jakarta.validation.Valid
import org.apache.juli.logging.Log
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/subcategories")
class SubcategoryWebController(
    private val subcategoryService: SubcategoryService,
    private val categoryRepository: CategoryRepository,
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) categoryId: Long?,
        model: Model
    ): String {
        val subcategories = if (categoryId != null) {
            subcategoryService.findByCategoryId(categoryId)
        } else {
            subcategoryService.findAll()
        }
        val categories = categoryRepository.findAll().sortedBy { it.position }

        model.addAttribute("subcategories", subcategories)
        model.addAttribute("categories", categories)
        model.addAttribute("selectedCategoryId", categoryId)
        return "pages/subcategories/list"
    }

    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        val newSubCategory = SubCategory(
            text = "",
            route = "",
            status = true
        )

        val categories = categoryRepository.findByStatus(true)

        if (categories.isEmpty()) {
            model.addAttribute("error", "No hay categorías activas. Crea una categoría primero.")
            return "redirect:/admin/categories/new"
        }

        model.addAttribute("subcategory", newSubCategory)
        model.addAttribute("categories", categories)
        return "pages/subcategories/form"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val subcategory = subcategoryService.findById(id)
        if (subcategory == null) {
            redirectAttributes.addFlashAttribute("error", "Subcategoría no encontrada")
            return "redirect:/admin/subcategories"
        }
        val categories = categoryRepository.findByStatus(true)
        model.addAttribute("subcategory", subcategory)
        model.addAttribute("categories", categories)

        return "pages/subcategories/form"
    }

    @PostMapping("/save")
    fun create(
        @Valid @ModelAttribute subcategory: SubCategory,
        @RequestParam("category.id") categoryId: Long,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val category = categoryRepository.findById(categoryId).orElse(null)

        if (category == null) {
            bindingResult.rejectValue("category", "error.subcategory", "Categoría no encontrada")
        } else {
            subcategory.category = category
        }

        val existingSubCategory = subcategoryService.findByRoute(subcategory.route)
        if (existingSubCategory != null) {
            bindingResult.rejectValue("route", "error.subcategory", "Ya existe una subcategoría con esta ruta")
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("subcategory", subcategory)
            model.addAttribute("categories", categoryRepository.findByStatus(true))
            return "pages/subcategories/form"
        }

        try {
            subcategoryService.save(subcategory)
            redirectAttributes.addFlashAttribute("success", "Subcategoría creada exitosamente")
            return "redirect:/admin/subcategories"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la subcategoría: ${e.message}")
            return "redirect:/admin/subcategories/new"
        }
    }


    @PostMapping("/update/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute subcategory: SubCategory,
        @RequestParam("category.id") categoryId: Long,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val existingSubCategory = subcategoryService.findById(id)

        if (existingSubCategory == null) {
            redirectAttributes.addFlashAttribute("error", "Subcategoría no encontrada")
            return "redirect:/admin/subcategories"
        }

        val category = categoryRepository.findById(categoryId).orElse(null)
        if (category == null) {
            bindingResult.rejectValue("category", "error.subcategory", "Categoría no encontrada")
        } else {
            subcategory.category = category
        }

        val subCategoryWithSameRoute = subcategoryService.findByRoute(subcategory.route)
        if (subCategoryWithSameRoute != null && subCategoryWithSameRoute.id != id) {
            bindingResult.rejectValue("route", "error.subcategory", "Ya existe una subcategoría con esta ruta")
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("subcategory", subcategory)
            model.addAttribute("categories", categoryRepository.findByStatus(true))
            return "pages/subcategories/form"
        }

        try {
            subcategoryService.update(id, subcategory)
            redirectAttributes.addFlashAttribute("success", "Subcategoría actualizada exitosamente")
            return "redirect:/admin/subcategories"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: ${e.message}")
            println(e.message)
            return "redirect:/admin/subcategories/edit/$id"
        }
    }

    @PostMapping("/delete/{id}")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            subcategoryService.deleteById(id)
            redirectAttributes.addFlashAttribute("success", "Subcategoría eliminada exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: ${e.message}")
        }
        return "redirect:/admin/subcategories"
    }

    @PostMapping("/toggle-status/{id}")
    fun toggleStatus(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String {
        try {
            subcategoryService.toggleStatus(id)
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado: ${e.message}")
        }
        return "redirect:/admin/subcategories"
    }

}
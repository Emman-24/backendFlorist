package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.service.SeoUrlService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/categories")
class CategoryWebController(
    private val categoryRepository: CategoryRepository,
    private val seoUrlService: SeoUrlService
) {

    @GetMapping
    fun list(model: Model): String {
        val categories = categoryRepository.findAll().sortedBy { it.position }
        model.addAttribute("categories", categories)
        return "pages/categories/list"
    }

    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        val newCategory = Category(
            text = "",
            route = "",
            status = true
        )
        model.addAttribute("category", newCategory)
        return "pages/categories/form"
    }

    @PostMapping("/save")
    fun create(
        @Valid @ModelAttribute category: Category,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            return "pages/categories/form"
        }
        try {
            val savedCategory = categoryRepository.save(category)
            seoUrlService.createOrUpdateCategoryUrl(savedCategory)
            redirectAttributes.addFlashAttribute("success", "Categoría guardada correctamente")
        } catch (e: Exception) {
            model.addAttribute("error", "Error al guardar la categoría: ${e.message}")
            return "pages/categories/form"
        }
        return "redirect:/admin/categories"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(@PathVariable id: Long, model: Model): String {
        val category = categoryRepository.findById(id).orElseThrow { RuntimeException("Categoría no encontrada") }
        model.addAttribute("category", category)
        return "pages/categories/form"
    }

    @PostMapping("/update/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute category: Category,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val existingCategory = categoryRepository.findById(id).orElse(null)

        if (existingCategory == null) {
            redirectAttributes.addFlashAttribute("error", "Categoría no encontrada")
            return "redirect:/admin/categories"
        }
        val categoryWithSameRoute = categoryRepository.findByRoute(category.route)

        if (categoryWithSameRoute != null && categoryWithSameRoute.id != id) {
            bindingResult.rejectValue("route", "error.category", "Ya existe una categoría con esta ruta")
        }
        if (bindingResult.hasErrors()) {
            category.id = id
            model.addAttribute("category", category)
            return "pages/categories/form"
        }
        try {
            existingCategory.text = category.text
            existingCategory.route = category.route
            existingCategory.description = category.description
            existingCategory.position = category.position
            existingCategory.status = category.status

            val updatedCategory = categoryRepository.save(existingCategory)
            seoUrlService.createOrUpdateCategoryUrl(updatedCategory)

            redirectAttributes.addFlashAttribute("success", "Categoría actualizada exitosamente")
            return "redirect:/admin/categories"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la categoría: ${e.message}")
            return "redirect:/categories/edit/$id"
        }
    }

    @PostMapping("/delete/{id}")
    fun deleteCategory(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val category = categoryRepository.findById(id).orElse(null)

        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Categoría no encontrada")
            return "redirect:/admin/categories"
        }

        if (category.subCategories.isNotEmpty() || category.products.isNotEmpty()) {
            redirectAttributes.addFlashAttribute(
                "error",
                "No se puede eliminar esta categoría porque tiene ${category.subCategories.size} subcategorías " +
                        "y ${category.products.size} productos asociados. Elimina o reasigna estos elementos primero."
            )
            return "redirect:/admin/categories"
        }
        try {
            categoryRepository.delete(category)
            redirectAttributes.addFlashAttribute("success", "Categoría eliminada correctamente")
        } catch (_: Exception) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la categoría")
        }
        return "redirect:/admin/categories"
    }

    @PostMapping("/toggle-status/{id}")
    fun toggleStatus(@PathVariable id: Long, redirectAttributes: RedirectAttributes): String {
        try {
            val category = categoryRepository.findById(id).orElseThrow { RuntimeException("Categoría no encontrada") }
            category.status = !category.status
            categoryRepository.save(category)
            redirectAttributes.addFlashAttribute("success", "Estado actualizado correctamente")
        } catch (_: Exception) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el estado")
        }
        return "redirect:/admin/categories"
    }
}
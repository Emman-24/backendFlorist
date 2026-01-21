package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.Tag
import com.floristeriaakasia.backend.service.TagService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin/tags")
class TagWebController(
    private val tagService: TagService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) search: String?,
        model: Model
    ): String {
        val tags = if (search.isNullOrBlank()) {
            tagService.findAll()
        } else {
            tagService.search(search)
        }.sortedByDescending { it.products.size }
        model.addAttribute("tags", tags)
        model.addAttribute("searchQuery", search)
        return "pages/tags/list"
    }

    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        val newTag = Tag(
            text = "",
            route = "",
            status = true
        )
        model.addAttribute("tag", newTag)
        return "pages/tags/form"
    }

    @PostMapping("/save")
    fun create(
        @Valid @ModelAttribute tag: Tag,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val existingTag = tagService.findByRoute(tag.route)
        if (existingTag != null) {
            bindingResult.rejectValue("route", "error.tag", "Tag ya existe")
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("tag", tag)
            return "pages/tags/form"
        }
        try {
            tagService.save(tag)
            redirectAttributes.addFlashAttribute("success", "Tag guardado correctamente")
            return "redirect:/admin/tags"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el tag: ${e.message}")
            return "redirect:/admin/tags/new"
        }
    }

    @PostMapping("/update/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute tag: Tag,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val existingTag = tagService.findById(id)
        if (existingTag == null) {
            redirectAttributes.addFlashAttribute("error", "Tag no encontrado")
            return "redirect:/admin/tags"
        }
        val tagWithSameRoute = tagService.findByRoute(tag.route)
        if (tagWithSameRoute != null && tagWithSameRoute.id != id) {
            bindingResult.rejectValue("route", "error.tag", "Ya existe un tag con esta ruta")
        }
        if (bindingResult.hasErrors()) {
            tag.id = id
            model.addAttribute("tag", tag)
            return "pages/tags/form"
        }
        try {
            tagService.update(id, tag)
            redirectAttributes.addFlashAttribute("success", "Tag actualizado correctamente")
            return "redirect:/admin/tags"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el tag: ${e.message}")
            return "redirect:/admin/tags/edit/$id"
        }

    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val tag = tagService.findById(id)
        if (tag == null) {
            redirectAttributes.addFlashAttribute("error", "Tag no encontrada")
            return "redirect:/admin/tags"
        }
        model.addAttribute("tag", tag)
        return "pages/tags/form"
    }

    @PostMapping("/toggle-status/{id}")
    fun toggleStatus(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            tagService.toggleStatus(id)
            val statusText = if (tagService.findById(id)?.status == true) "activado" else "desactivado"
            redirectAttributes.addFlashAttribute("success", "Tag $statusText correctamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el tag: ${e.message}")
        }
        return "redirect:/admin/tags"
    }

    @PostMapping("/delete/{id}")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ):String{
        val tag = tagService.findById(id)

        if (tag == null) {
            redirectAttributes.addFlashAttribute("error", "Tag no encontrada")
            return "redirect:/admin/tags"
        }
        try {
            tagService.deleteById(id)
            redirectAttributes.addFlashAttribute("success", "Tag eliminado exitosamente${if (tag.products.isNotEmpty()) ". Se removió de ${tag.products.size} productos" else ""}")
        }catch (e: Exception){
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el tag: ${e.message}")
        }
        return "redirect:/admin/tags"
    }

    @GetMapping("/api/most-used")
    @ResponseBody
    fun getMostUsedTags(@RequestParam(defaultValue = "10") limit: Int): List<Map<String, Any>> {
        return tagService.getMostUsedTags(limit).map { stats ->
            mapOf(
                "id" to stats.id,
                "name" to stats.name,
                "route" to stats.route,
                "productCount" to stats.productCount,
                "status" to stats.status
            )
        }
    }

}

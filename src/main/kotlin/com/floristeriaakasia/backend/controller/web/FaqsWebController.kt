package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.Faq
import com.floristeriaakasia.backend.service.FaqService
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/faqs")
class FaqsWebController(
    private val faqService: FaqService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) search: String?,
        model: Model
    ): String {
        val faqs = faqService.searchWithFilters(search, category)

        model.addAttribute("faqs", faqs)
        model.addAttribute("selectedCategory", category)
        model.addAttribute("searchQuery", search)

        return "pages/faqs/list"
    }


    @GetMapping("/new")
    fun showCreateForm(model: Model): String {
        val newFaq = Faq(
            question = "",
            answer = "",
            category = "",
            position = 0,
            status = true
        )
        model.addAttribute("faq", newFaq)
        return "pages/faqs/form"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model, redirectAttributes: RedirectAttributes
    ): String {
        val faq = faqService.findById(id)

        if (faq == null) {
            redirectAttributes.addFlashAttribute("error", "FAQ no encontrada")
            return "redirect:/faqs"
        }

        model.addAttribute("faq", faq)
        return "pages/faqs/form"
    }

    @PostMapping("/save")
    fun create(
        @Valid @ModelAttribute faq: Faq,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("faq", faq)
            return "pages/faqs/form"
        }
        try {
            faqService.save(faq)
            redirectAttributes.addFlashAttribute("success", "FAQ guardada correctamente")
            return "redirect:/faqs"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la FAQ: ${e.message}")
            return "pages/faqs/form"
        }
    }

    @PostMapping("/update/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @ModelAttribute faq: Faq,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        val existingFaq = faqService.findById(id)

        if (existingFaq == null) {
            redirectAttributes.addFlashAttribute("error", "FAQ no encontrada")
            return "redirect:/faqs"
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("faq", faq)
            return "pages/faqs/form"
        }
        try {
            faqService.update(id, faq)
            redirectAttributes.addFlashAttribute("success", "FAQ actualizada correctamente")
            return "redirect:/faqs"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la FAQ: ${e.message}")
            return "pages/faqs/form"
        }
    }

    @PostMapping("/delete/{id}")
    fun delete(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        val faq = faqService.findById(id)

        if (faq == null) {
            redirectAttributes.addFlashAttribute("error", "FAQ no encontrada")
            return "redirect:/faqs"
        }

        try {
            faqService.deleteById(id)
            redirectAttributes.addFlashAttribute("success", "FAQ eliminada correctamente")
            return "redirect:/faqs"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la FAQ: ${e.message}")
            return "redirect:/faqs"
        }
    }

    @PostMapping("/toggle-status/{id}")
    fun toggleStatus(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            val faq = faqService.toggleStatus(id)
            val statusText = if (faq.status) "activada" else "desactivada"
            redirectAttributes.addFlashAttribute("success", "FAQ $statusText exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado: ${e.message}")
        }
        return "redirect:/faqs"
    }


}
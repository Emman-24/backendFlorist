package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.dto.tag.TagRequest
import com.floristeriaakasia.backend.service.TagService
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/tags")
class TagWebController(
    private val service: TagService
) {

    @GetMapping
    fun showTagList(model: Model): String {
        model.addAttribute("tags", service.findAll())
        model.addAttribute("tagCreateRequest", TagRequest("", "", true))
        return "pages/tags/list"
    }

    @PostMapping
    fun createTag(
        @Valid @ModelAttribute("tagCreateRequest") request: TagRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tags", service.findAll())
            return "pages/tags/list"
        }

        return try {
            service.create(request)
            "redirect:/tags"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.rejectValue("text", "error.tag", "Name or Route already exists.")
            model.addAttribute("tags", service.findAll())
            "pages/tags/list"
        }

    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute("tagCreateRequest", service.findRequestById(id))
        model.addAttribute("tagId", id)
        return "pages/tags/edit"
    }

    @PostMapping("/update/{id}")
    fun updateTag(
        @PathVariable id: Long,
        @Valid @ModelAttribute("tagCreateRequest") request: TagRequest,
        bindingResult: BindingResult,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tagId", id)
            return "pages/tags/edit"
        }
        return try {
            service.update(id, request)
            "redirect:/tags"
        } catch (_: DataIntegrityViolationException) {
            bindingResult.rejectValue("text", "error.tag", "Name or Route already exists.")
            model.addAttribute("tagId", id)
            "pages/tags/edit"
        }
    }

    @PostMapping("/delete/{id}")
    fun deleteTag(@PathVariable id: Long): String {
        service.deleteById(id)
        return "redirect:/tags"
    }

}
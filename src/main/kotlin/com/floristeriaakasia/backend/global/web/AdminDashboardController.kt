package com.floristeriaakasia.backend.global.web

import com.floristeriaakasia.backend.feature.dashboard.application.GetAdminDashboardSummaryUseCase
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminDashboardController(
    private val getAdminDashboardSummaryUseCase: GetAdminDashboardSummaryUseCase
) {
    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        populateModel(model)
        return "admin/dashboard"
    }

    @GetMapping("/dashboard/summary-cards")
    fun summaryCards(model: Model): String {
        populateModel(model)
        return "admin/dashboard :: summaryCards"
    }

    private fun populateModel(model: Model) {
        val auth = SecurityContextHolder.getContext().authentication
        model.addAttribute("activePage", "dashboard")
        model.addAttribute("username", auth?.name ?: "Admin")
        model.addAttribute("summary", getAdminDashboardSummaryUseCase.execute())
    }
}
package com.floristeriaakasia.backend.controller.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class DashboardController {

    @GetMapping("/")
    fun getDashboard(model: Model): String {
        model.addAttribute("pageTitle", "Dashboard Home")
        return "pages/home"
    }

    @GetMapping("/users")
    fun getUsers(model: Model): String {
        model.addAttribute("pageTitle", "User Management")
        model.addAttribute("userList", listOf("Alice", "Bob", "Charlie"))
        return "pages/users"
    }

    @GetMapping("/settings")
    fun getSettings(model: Model): String {
        model.addAttribute("pageTitle", "Application Settings")
        return "pages/settings"
    }

}
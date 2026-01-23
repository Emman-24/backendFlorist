package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.User
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller

class DashboardController {

    @GetMapping("/")
    fun getDashboard(@AuthenticationPrincipal user: User, model: Model): String {
        val userName = user.fullName ?: user.username
        model.addAttribute("pageTitle", userName)
        return "pages/home"
    }

    @GetMapping("/admin/users")
    fun getUsers(model: Model): String {
        model.addAttribute("pageTitle", "User Management")
        model.addAttribute("userList", listOf("Alice", "Bob", "Charlie"))
        return "pages/users"
    }

    @GetMapping("/admin/settings")
    fun getSettings(model: Model): String {
        model.addAttribute("pageTitle", "Application Settings")
        return "pages/settings"
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): String {
        val accessTokenCookie = Cookie("accessToken", null).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
        }
        response.addCookie(accessTokenCookie)
        
        val refreshTokenCookie = Cookie("refreshToken", null).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
        }
        response.addCookie(refreshTokenCookie)
        
        return "redirect:/login?logout"
    }

}
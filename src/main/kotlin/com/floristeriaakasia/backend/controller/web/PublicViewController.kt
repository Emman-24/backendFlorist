package com.floristeriaakasia.backend.controller.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class PublicViewController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }

}
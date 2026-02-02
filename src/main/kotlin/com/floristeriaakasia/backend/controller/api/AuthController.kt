package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.dto.RegisterRequestDTO
import com.floristeriaakasia.backend.service.AuthResponse
import com.floristeriaakasia.backend.service.AuthService
import com.floristeriaakasia.backend.service.LoginRequest
import com.floristeriaakasia.backend.service.RegisterRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null)
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequestDTO): ResponseEntity<Any> {
        return try {
            val registerRequest = RegisterRequest(
                username = request.username,
                email = request.email,
                password = request.password,
                fullName = request.fullName
            )
            val response = authService.register(registerRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Any> {
        return try {
            val response = authService.refreshToken(request.refreshToken)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }

}

data class RefreshTokenRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String
)

data class ValidateTokenRequest(
    @field:NotBlank(message = "El token es obligatorio")
    val token: String
)
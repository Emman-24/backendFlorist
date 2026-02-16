package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.dto.LoginRequestDTO
import com.floristeriaakasia.backend.model.dto.RegisterRequestDTO

import com.floristeriaakasia.backend.service.AuthService
import com.floristeriaakasia.backend.util.HtmlSanitizer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequestDTO
    ): ResponseEntity<Any> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (_: BadCredentialsException) {
            val sanitizedUsername = HtmlSanitizer.sanitizeUsername(request.username) ?: "invalid"
            logger.warn("Failed login attempt for username: {}", sanitizedUsername)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid username or password"))
        } catch (e: IllegalArgumentException) {
            val sanitizedUsername = HtmlSanitizer.sanitizeUsername(request.username) ?: "invalid"
            logger.warn("Login failed for username: {}", sanitizedUsername)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            val sanitizedUsername = HtmlSanitizer.sanitizeUsername(request.username) ?: "invalid"
            logger.error("Unexpected error during login for username: $sanitizedUsername", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Authentication service unavailable"))
        }
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequestDTO): ResponseEntity<Any> {
        return try {
            if (HtmlSanitizer.containsHtml(request.username) ||
                HtmlSanitizer.containsHtml(request.email) ||
                HtmlSanitizer.containsHtml(request.fullName)) {
                logger.warn("XSS attempt detected in registration: {}", request.username)
                return ResponseEntity.badRequest()
                    .body(mapOf("error" to "Invalid characters detected in input"))
            }

            val sanitizedUsername = HtmlSanitizer.sanitizeUsername(request.username)
                ?: throw IllegalArgumentException("Invalid username")
            val sanitizedEmail = HtmlSanitizer.sanitizeEmail(request.email)
                ?: throw IllegalArgumentException("Invalid email")
            val sanitizedFullName = HtmlSanitizer.sanitizeText(request.fullName)

            val sanitizedRequest = request.copy(
                username = sanitizedUsername,
                email = sanitizedEmail,
                fullName = sanitizedFullName
            )

            val response = authService.register(sanitizedRequest)
            logger.info("User registered successfully: {}", sanitizedUsername)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Registration failed: {}", e.message)
            ResponseEntity.badRequest()
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during registration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Registration service unavailable"))
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Any> {
        return try {
            val response = authService.refreshToken(request.refreshToken)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Token refresh failed: {}", e.message)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during token refresh", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Token refresh service unavailable"))
        }
    }

}

data class RefreshTokenRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String
)


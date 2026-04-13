package com.floristeriaakasia.backend.feature.user

import com.floristeriaakasia.backend.global.security.JWTAuthenticationFilter
import com.floristeriaakasia.backend.global.security.JwtService
import com.floristeriaakasia.backend.global.security.JwtTokenBlacklist
import com.floristeriaakasia.backend.util.HtmlSanitizer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val tokenBlacklist: JwtTokenBlacklist,
    private val jwtFilter: JWTAuthenticationFilter
) {
    private val logger = LoggerFactory.getLogger(javaClass)


    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDTO): ResponseEntity<Any> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (_: BadCredentialsException) {
            logger.warn("AUTH_FAILED username={}", HtmlSanitizer.sanitizeUsername(request.username))
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                    mapOf(
                        "success" to false, "code" to "BAD_CREDENTIALS",
                        "message" to "Invalid username or password"
                    )
                )
        } catch (e: IllegalArgumentException) {
            logger.warn("AUTH_FAILED reason={}", e.message)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "code" to "AUTH_ERROR", "message" to e.message))
        } catch (e: Exception) {
            logger.error("AUTH_UNEXPECTED_ERROR", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    mapOf(
                        "success" to false, "code" to "SERVICE_UNAVAILABLE",
                        "message" to "Authentication service unavailable"
                    )
                )
        }
    }
}

//    @PostMapping("/register")
//    fun register(@Valid @RequestBody request: RegisterRequestDTO): ResponseEntity<Any> {
//        return try {
//            if (HtmlSanitizer.containsHtml(request.username) ||
//                HtmlSanitizer.containsHtml(request.email) ||
//                HtmlSanitizer.containsHtml(request.fullName)) {
//                logger.warn("SEC_EVENT=XSS_REGISTER_ATTEMPT username={}",
//                    HtmlSanitizer.sanitizeUsername(request.username))
//                return ResponseEntity.badRequest()
//                    .body(mapOf("success" to false, "code" to "INVALID_INPUT",
//                        "message" to "Invalid characters detected in input"))
//            }
//            val sanitized = request.copy(
//                username = HtmlSanitizer.sanitizeUsername(request.username)
//                    ?: throw IllegalArgumentException("Invalid username"),
//                email    = HtmlSanitizer.sanitizeEmail(request.email)
//                    ?: throw IllegalArgumentException("Invalid email"),
//                fullName = HtmlSanitizer.sanitizeText(request.fullName)
//            )
//            val response = authService.register(sanitized)
//            logger.info("AUTH_REGISTER_OK username={}", sanitized.username)
//            ResponseEntity.status(HttpStatus.CREATED).body(response)
//        } catch (e: IllegalArgumentException) {
//            logger.warn("REGISTER_FAILED reason={}", e.message)
//            ResponseEntity.badRequest()
//                .body(mapOf("success" to false, "code" to "REGISTER_ERROR", "message" to e.message))
//        } catch (e: Exception) {
//            logger.error("REGISTER_UNEXPECTED_ERROR", e)
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(mapOf("success" to false, "code" to "SERVICE_UNAVAILABLE",
//                    "message" to "Registration service unavailable"))
//        }
//    }

//    @PostMapping("/refresh")
//    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<Any> {
//        return try {
//            val response = authService.refreshToken(request.refreshToken)
//            ResponseEntity.ok(response)
//        } catch (e: IllegalArgumentException) {
//            logger.warn("TOKEN_REFRESH_FAILED reason={}", e.message)
//            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(mapOf("success" to false, "code" to "INVALID_REFRESH_TOKEN",
//                    "message" to e.message))
//        } catch (e: Exception) {
//            logger.error("TOKEN_REFRESH_UNEXPECTED_ERROR", e)
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(mapOf("success" to false, "code" to "SERVICE_UNAVAILABLE",
//                    "message" to "Token refresh service unavailable"))
//        }
//    }
//
//    @PostMapping("/logout")
//    fun logout(
//        @RequestHeader("Authorization") authHeader: String,
//        @AuthenticationPrincipal principal: UserDetails?
//    ): ResponseEntity<Any> {
//        val token = authHeader.removePrefix("Bearer ").trim()
//        val ttl = jwtService.getRemainingTtl(token)
//        tokenBlacklist.revoke(token, ttl)
//        principal?.username?.let { jwtFilter.evictUser(it) }
//        logger.info("AUTH_LOGOUT_OK user={} ttlMs={}", principal?.username, ttl)
//        return ResponseEntity.ok(
//            mapOf("success" to true, "message" to "Logged out successfully")
//        )
//    }
//}

//data class RefreshTokenRequest(
//    @field:NotBlank(message = "El refresh token es obligatorio")
//    val refreshToken: String
//)
package com.floristeriaakasia.backend.global.config

import com.floristeriaakasia.backend.global.security.JWTAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Instant

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
class SecurityConfig(
    private val jwtAuthFilter: JWTAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
    @param:Value("\${app.cors.allowed-origins}") private val allowedOrigins: List<String>
) {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    companion object {
        // Public endpoints patterns (pre-compiled for performance)
        private val PUBLIC_ENDPOINTS = arrayOf(
            "swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/login",
            "/api/auth/register",
            "/api/floral-arrangement",
            "/api/floral-arrangement/*",
            "/api/floral-arrangement/*/image",
            "/api/floral-arrangement/slug/*",
            "/api/categories",
            "/api/categories/*",
            "/api/categories/*/*",
            "/api/tags",
            "/api/tags/*",
            "/api/tags/*/*",
            "/api/faqs",
            "/api/faqs/*",
            "/error"
        )

        private val ADMIN_ENDPOINTS = arrayOf(
            "/api/admin/**",
            "/actuator/metrics",
            "/actuator/prometheus"
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("Configuring security filter chain with enhanced headers and rate limiting")
        http
            .csrf { it.disable() }

            .cors { it.configurationSource(corsConfigurationSource()) }

            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            .headers { headers ->
                headers
                    .xssProtection { xss ->
                        xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                    }

                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives(
                            "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline'; " +
                            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                            "font-src 'self' https://fonts.gstatic.com; " +
                            "img-src 'self' data: https: blob:; " +
                            "connect-src 'self' https://res.cloudinary.com; " +
                            "frame-ancestors 'none'; " +
                            "form-action 'self'; " +
                            "upgrade-insecure-requests;"
                        )
                    }

                    .contentTypeOptions { }

                    .frameOptions { frame -> frame.deny() }

                    .httpStrictTransportSecurity { hsts ->
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000) // 1 year
                    }

                    .referrerPolicy { referrer ->
                        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
            }

            .sessionManagement { session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .sessionFixation().none()
            }


            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(*PUBLIC_ENDPOINTS).permitAll()

                    .requestMatchers(*ADMIN_ENDPOINTS).hasAnyRole("ADMIN", "MANAGER")

                    .anyRequest().authenticated()
            }


            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, authException ->
                        val clientIp = getClientIp(request)
                        logger.warn(
                            "Authentication failed: ip={}, uri={}, error={}",
                            clientIp,
                            request.requestURI,
                            authException.message
                        )

                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.contentType = "application/json"
                        response.characterEncoding = "UTF-8"
                        response.writer.write(
                            """{"success":false,"message":"Authentication required","timestamp":"${Instant.now()}"}"""
                        )
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        val clientIp = getClientIp(request)
                        val username = request.userPrincipal?.name ?: "anonymous"
                        logger.warn(
                            "Access denied: user={}, ip={}, uri={}, error={}",
                            username,
                            clientIp,
                            request.requestURI,
                            accessDeniedException.message
                        )

                        response.status = HttpServletResponse.SC_FORBIDDEN
                        response.contentType = "application/json"
                        response.characterEncoding = "UTF-8"
                        response.writer.write(
                            """{"success":false,"message":"Access denied - insufficient permissions","timestamp":"${Instant.now()}"}"""
                        )
                    }
            }

            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(jwtAuthFilter, RateLimitFilter::class.java)

        logger.info("Security filter chain configured successfully")
        return http.build()
    }


    private fun getClientIp(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        return when {
            !forwardedFor.isNullOrBlank() -> forwardedFor.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp
            else -> request.remoteAddr
        } ?: "unknown"
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        logger.info("Configuring CORS for origins: {}", allowedOrigins)

        val configuration = CorsConfiguration().apply {
            allowedOrigins = this@SecurityConfig.allowedOrigins

            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")

            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control",
                "X-CSRF-TOKEN"
            )

            exposedHeaders = listOf(
                "Authorization",
                "X-Total-Count",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset",
                "Content-Disposition"
            )

            allowCredentials = true

            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }


    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

}
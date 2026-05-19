package com.floristeriaakasia.backend.global.config

import com.floristeriaakasia.backend.global.security.JWTAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
    @Value("\${app.cors.allowed-origins}")
    private val rawAllowedOrigins: String,
    @Value("\${app.security.environment:production}") private val environment: String
) {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    companion object {

        private val STATIC_PATTERNS = arrayOf(
            "/robots.txt",
            "/favicon.ico",
            "/sitemap.xml",
            "/sitemap*.xml",
        )

        private val PUBLIC_GET_PATTERNS = arrayOf(
            "/api/floral-arrangement",
            "/api/floral-arrangement/*",
            "/api/floral-arrangement/slug/*",
            "/api/floral-arrangement/seo-name/*",
            "/api/categories",
            "/api/categories/*",
            "/api/categories/*/children",
            "/api/categories/tree",
            "/api/tags",
            "/api/tags/*",
            "/api/faqs",
            "/api/faqs/*"
        )

        private val AUTH_PATTERNS = arrayOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh"
        )

        private val INFRA_PATTERNS = arrayOf(
            "/",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/health",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/error"
        )

        private val ADMIN_PATTERNS = arrayOf(
            "/api/admin/**",
            "/actuator/metrics",
            "/actuator/prometheus",
            "/actuator/env",
            "/actuator/beans",
            "/actuator/loggers/**"
        )

        private val MANAGER_PATTERNS = arrayOf(
            "/api/faqs/admin",
            "/api/tags/product/**"
        )

    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("Configuring security filter chain [env={}]", environment)

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
                        csp.policyDirectives(buildCsp())
                    }

                    .contentTypeOptions { }

                    .frameOptions { frame -> frame.deny() }

                    .httpStrictTransportSecurity { hsts ->
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                            .preload(true)
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

                    .requestMatchers(*STATIC_PATTERNS).permitAll()

                    .requestMatchers(*INFRA_PATTERNS).permitAll()

                    .requestMatchers(*AUTH_PATTERNS).permitAll()

                    .requestMatchers(HttpMethod.GET, *PUBLIC_GET_PATTERNS).permitAll()

                    .requestMatchers(*ADMIN_PATTERNS).hasRole("ADMIN")

                    .requestMatchers(*MANAGER_PATTERNS).hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST, "/api/floral-arrangement/*/image")

                    .hasAnyRole("ADMIN", "MANAGER")

                    .requestMatchers(HttpMethod.POST, "/api/floral-arrangement")

                    .hasAnyRole("ADMIN", "MANAGER")

                    .anyRequest().authenticated()
            }

            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, authException ->
                        logSecurityEvent("AUTHN_FAILED", request, authException.message)
                        writeJsonError(
                            response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Authentication required",
                            "UNAUTHORIZED"
                        )
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        val user = request.userPrincipal?.name ?: "anonymous"
                        logSecurityEvent("AUTHZ_DENIED [$user]", request, accessDeniedException.message)
                        writeJsonError(
                            response,
                            HttpServletResponse.SC_FORBIDDEN,
                            "Access denied — insufficient permissions",
                            "FORBIDDEN"
                        )
                    }
            }

            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)

            .addFilterAfter(jwtAuthFilter, RateLimitFilter::class.java)

        logger.info("Security filter chain ready [env={}]", environment)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {

        val configuredOrigins = rawAllowedOrigins
            .split(",")
            .map(String::trim)
            .filter(String::isNotBlank)

        val effectiveOrigins = if (environment == "production") {
            configuredOrigins
        } else {
            (configuredOrigins + listOf(
                "http://localhost:4200",
                "http://localhost:3000",
                "http://localhost:8080"
            )).distinct()
        }

        logger.info("CORS effective origins: {}", effectiveOrigins)

        val config = CorsConfiguration().apply {
            allowedOrigins = effectiveOrigins
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
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset",
                "Content-Disposition"
            )
            allowCredentials = true
            maxAge = 3_600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    private fun buildCsp(): String {
        val base = listOf(
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' https://fonts.gstatic.com",
            "img-src 'self' data: https://res.cloudinary.com blob:",
            "connect-src 'self' https://res.cloudinary.com",
            "media-src 'none'",
            "object-src 'none'",
            "frame-src 'none'",
            "frame-ancestors 'none'",
            "form-action 'self'",
            "base-uri 'self'",
            "upgrade-insecure-requests"
        )
        val swaggerAddition = if (environment != "production") {
            listOf("script-src 'self' 'unsafe-inline' https://unpkg.com https://cdnjs.cloudflare.com")
        } else emptyList()

        return (if (swaggerAddition.isEmpty()) base else base.dropLast(1) + swaggerAddition + base.last())
            .joinToString("; ")
    }

    private fun logSecurityEvent(event: String, request: HttpServletRequest, detail: String?) {
        val ip = extractClientIp(request)
        logger.warn("SEC_EVENT={} ip={} uri={} detail={}", event, ip, request.requestURI, detail)
    }

    private fun writeJsonError(
        response: HttpServletResponse,
        status: Int,
        message: String,
        code: String
    ) {
        response.status = status
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """{"success":false,"code":"$code","message":"$message","timestamp":"${Instant.now()}"}"""
        )
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xff = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        val cfIp = request.getHeader("CF-Connecting-IP")
        return when {
            !cfIp.isNullOrBlank() -> cfIp.trim()
            !xff.isNullOrBlank() -> xff.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp.trim()
            else -> request.remoteAddr
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
package com.floristeriaakasia.backend.global.config

import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.RateLimiter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter : OncePerRequestFilter() {

    companion object {

        private const val RATE_LIMIT_HEADER = "X-RateLimit-Remaining"
        private const val RATE_LIMIT_RESET = "X-RateLimit-Reset"
        private const val RATE_LIMIT_LIMIT = "X-RateLimit-Limit"

        private const val PUBLIC_ENDPOINT_RATE = 100.0
        private const val AUTH_ENDPOINT_RATE = 10.0
        private const val UPLOAD_ENDPOINT_RATE = 2.0
        private const val ADMIN_ENDPOINT_RATE = 50.0

        private const val LIMITER_CACHE_SIZE = 10000L
        private const val LIMITER_EXPIRATION_MINUTES = 15L
        private const val ACQUIRE_TIMEOUT_MS = 100L
    }

    // Cache of rate limiters per client IP
    private val limiters = CacheBuilder.newBuilder()
        .expireAfterAccess(LIMITER_EXPIRATION_MINUTES, TimeUnit.MINUTES)
        .maximumSize(LIMITER_CACHE_SIZE)
        .recordStats()
        .build<String, RateLimiter>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = getClientIp(request)
        val endpoint = request.requestURI
        val method = request.method

        // Determine rate limit based on endpoint
        val rate = determineRateLimit(endpoint, method)

        // Create unique key: IP + rate tier (allows different limits per endpoint type)
        val clientKey = "$clientIp:${rate.toInt()}"

        // Get or create rate limiter for this client
        val limiter = limiters.get(clientKey) {
            logger.debug("Creating rate limiter: client=$clientIp, rate=$rate req/s")
            RateLimiter.create(rate)
        }

        // Try to acquire permit
        if (!limiter.tryAcquire(ACQUIRE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            handleRateLimitExceeded(request, response, clientIp, endpoint, rate)
            return
        }

        // Add rate limit headers
        addRateLimitHeaders(response, limiter, rate)

        // Continue filter chain
        filterChain.doFilter(request, response)
    }

    /**
     * Determine rate limit based on endpoint and method
     */
    private fun determineRateLimit(endpoint: String, method: String): Double {
        return when {
            // Auth endpoints - strict limit (brute-force protection)
            endpoint.startsWith("/api/auth/") -> AUTH_ENDPOINT_RATE

            // Image upload endpoints - bandwidth protection
            endpoint.contains("/images") || endpoint.contains("/with-images") ->
                UPLOAD_ENDPOINT_RATE

            // Admin endpoints - moderate limit
            endpoint.startsWith("/api/admin/") -> ADMIN_ENDPOINT_RATE

            // Mutating operations (POST, PUT, DELETE) - moderate limit
            method in setOf("POST", "PUT", "PATCH", "DELETE") -> ADMIN_ENDPOINT_RATE

            // Public read endpoints - generous limit
            else -> PUBLIC_ENDPOINT_RATE
        }
    }

    /**
     * Handle rate limit exceeded
     */
    private fun handleRateLimitExceeded(
        request: HttpServletRequest,
        response: HttpServletResponse,
        clientIp: String,
        endpoint: String,
        rate: Double
    ) {
        logger.warn(
            "Rate limit exceeded: ip=$clientIp, endpoint=$endpoint, method=${request.method}, rate=$rate req/s"
        )

        response.status = 429
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val resetTimeMs = (1000 / rate).toLong()
        response.setHeader(RATE_LIMIT_LIMIT, rate.toInt().toString())
        response.setHeader(RATE_LIMIT_HEADER, "0")
        response.setHeader(RATE_LIMIT_RESET, resetTimeMs.toString())
        response.setHeader("Retry-After", (resetTimeMs / 1000).toString())

        val errorJson = """
            {
                "success": false,
                "timestamp": "${Instant.now()}",
                "status": 429,
                "error": "Too Many Requests",
                "message": "Rate limit exceeded. Please try again in ${resetTimeMs}ms.",
                "path": "${request.requestURI}",
                "limit": ${rate.toInt()},
                "retryAfter": $resetTimeMs
            }
        """.trimIndent()

        response.writer.write(errorJson)
    }

    /**
     * Add rate limit headers to response
     */
    private fun addRateLimitHeaders(
        response: HttpServletResponse,
        limiter: RateLimiter,
        rate: Double
    ) {
        val resetTimeMs = (1000 / limiter.rate).toLong()
        response.setHeader(RATE_LIMIT_LIMIT, rate.toInt().toString())
        response.setHeader(RATE_LIMIT_HEADER, limiter.rate.toInt().toString())
        response.setHeader(RATE_LIMIT_RESET, resetTimeMs.toString())
    }

    /**
     * Extract client IP with proxy support
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        val cfConnectingIp = request.getHeader("CF-Connecting-IP") // Cloudflare

        return when {
            !cfConnectingIp.isNullOrBlank() -> cfConnectingIp.trim()
            !forwardedFor.isNullOrBlank() -> forwardedFor.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp.trim()
            else -> request.remoteAddr
        } ?: "unknown"
    }

}

package com.floristeriaakasia.backend.global.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.RateLimiter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val HEADER_LIMIT = "X-RateLimit-Limit"
        private const val HEADER_REMAINING = "X-RateLimit-Remaining"
        private const val HEADER_RESET = "X-RateLimit-Reset"

        private const val RATE_AUTH = 5.0
        private const val RATE_UPLOAD = 2.0
        private const val RATE_ADMIN = 30.0
        private const val RATE_WRITE = 20.0
        private const val RATE_PUBLIC = 100.0

        private const val CACHE_SIZE = 20_000L
        private const val CACHE_TTL_MIN = 15L
        private const val ACQUIRE_TIMEOUT_MS = 50L
    }

    private val limiters = Caffeine.newBuilder()
        .maximumSize(CACHE_SIZE)
        .expireAfterAccess(CACHE_TTL_MIN, TimeUnit.MINUTES)
        .recordStats()
        .build<String, RateLimiter>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.method == "OPTIONS") {
            filterChain.doFilter(request, response)
            return
        }
        val clientIp = extractClientIp(request)
        val rate = determineRate(request.requestURI, request.method)
        val cacheKey = "$clientIp:${rate.toInt()}"

        val limiter = limiters.get(cacheKey) {
            RateLimiter.create(rate)
        }!!

        if (!limiter.tryAcquire(ACQUIRE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            handleExceeded(request, response, clientIp, rate)
            return
        }

        addRateLimitHeaders(response, rate)
        filterChain.doFilter(request, response)
    }

    private fun determineRate(uri: String, method: String): Double = when {
        uri.startsWith("/api/auth/") -> RATE_AUTH
        uri.contains("/image") || uri.contains("/images") -> RATE_UPLOAD
        uri.startsWith("/api/admin/") -> RATE_ADMIN
        method in setOf("POST", "PUT", "PATCH", "DELETE") -> RATE_WRITE
        else -> RATE_PUBLIC
    }

    private fun handleExceeded(
        request: HttpServletRequest,
        response: HttpServletResponse,
        clientIp: String,
        rate: Double
    ) {
        log.warn(
            "RATE_LIMIT_EXCEEDED ip={} uri={} method={} limit={}rps",
            clientIp, request.requestURI, request.method, rate.toInt()
        )

        val retryAfterSec = (1.0 / rate).toLong().coerceAtLeast(1L)
        val resetMs = retryAfterSec * 1000

        response.status = 429
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.setHeader(HEADER_LIMIT, rate.toInt().toString())
        response.setHeader(HEADER_REMAINING, "0")
        response.setHeader(HEADER_RESET, resetMs.toString())
        response.setHeader("Retry-After", retryAfterSec.toString())

        response.writer.write(
            """
            {
              "success": false,
              "code": "RATE_LIMIT_EXCEEDED",
              "message": "Too many requests. Please retry after ${retryAfterSec}s.",
              "retryAfterSeconds": $retryAfterSec,
              "timestamp": "${Instant.now()}",
              "path": "${request.requestURI}"
            }
            """.trimIndent()
        )
    }

    private fun addRateLimitHeaders(response: HttpServletResponse, rate: Double) {
        val resetMs = (1_000 / rate).toLong()
        response.setHeader(HEADER_LIMIT, rate.toInt().toString())
        response.setHeader(HEADER_REMAINING, rate.toInt().toString())
        response.setHeader(HEADER_RESET, resetMs.toString())
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val cf = request.getHeader("CF-Connecting-IP")
        val xff = request.getHeader("X-Forwarded-For")
        val ri = request.getHeader("X-Real-IP")
        return when {
            !cf.isNullOrBlank() -> cf.trim()
            !xff.isNullOrBlank() -> xff.split(",").first().trim()
            !ri.isNullOrBlank() -> ri.trim()
            else -> request.remoteAddr ?: "unknown"
        }
    }
}

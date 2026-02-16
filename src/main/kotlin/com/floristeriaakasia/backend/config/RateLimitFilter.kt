package com.floristeriaakasia.backend.config

import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.RateLimiter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter: OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val limiters = CacheBuilder.newBuilder()
        .expireAfterAccess(15, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build<String, RateLimiter>()

    private val publicEndpointRate = 100.0
    private val authEndpointRate = 10.0
    private val uploadEndpointRate = 2.0

    companion object {
        private const val RATE_LIMIT_HEADER = "X-RateLimit-Remaining"
        private const val RATE_LIMIT_RESET = "X-RateLimit-Reset"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientKey = getClientKey(request)
        val endpoint = request.requestURI

        val rate = when{
            endpoint.startsWith("/api/auth/") -> authEndpointRate
            endpoint.contains("/images") -> uploadEndpointRate
            else -> publicEndpointRate
        }

        val limiter = limiters.get(clientKey) {
            logger.info("Creating rate limiter for client: $clientKey with rate: $rate")
            RateLimiter.create(rate)
        }

        if (!limiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {

            logger.warn("Rate limit exceeded for client: $clientKey on endpoint: $endpoint")

            response.status = 429
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            val resetTime = (1000 / limiter.rate).toLong()
            response.setHeader(RATE_LIMIT_RESET, resetTime.toString())

            val errorJson = """
                {
                    "timestamp": "${java.time.Instant.now()}",
                    "status": 429,
                    "error": "Too Many Requests",
                    "message": "Has excedido el límite de solicitudes. Intenta de nuevo en unos segundos.",
                    "path": "${request.requestURI}"
                }
            """.trimIndent()

            response.writer.write(errorJson)
            return
        }

        response.setHeader(RATE_LIMIT_HEADER, limiter.rate.toInt().toString())
        val resetTime = (1000 / limiter.rate).toLong()
        response.setHeader(RATE_LIMIT_RESET, resetTime.toString())
        filterChain.doFilter(request, response)
    }

    private fun getClientKey(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        return when {
            !forwardedFor.isNullOrBlank() -> forwardedFor.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp
            else -> request.remoteAddr
        } ?: "unknown"
    }

}

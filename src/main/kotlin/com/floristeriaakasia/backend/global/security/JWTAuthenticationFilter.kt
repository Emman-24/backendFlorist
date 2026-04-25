package com.floristeriaakasia.backend.global.security

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class JWTAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val tokenBlacklist: JwtTokenBlacklist
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION_HEADER = "Authorization"

        private val STATIC_RESOURCES = setOf(
            "/robots.txt",
            "/favicon.ico",
            "/sitemap.xml"
        )

        private val PUBLIC_PREFIXES = setOf(
            "/api/auth/",
            "/actuator/health",
            "/actuator/info",
            "/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/error"
        )

        private val PUBLIC_GET_PREFIXES = setOf(
            "/api/floral-arrangement",
            "/api/categories",
            "/api/tags",
            "/api/faqs"
        )
    }

    private val userDetailsCache: Cache<String, UserDetails> = Caffeine.newBuilder()
        .maximumSize(2_000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        if (STATIC_RESOURCES.any { path == it || path.startsWith(it) }) return true

        if (!path.startsWith("/api/") &&
            !path.startsWith("/actuator/") &&
            !path.startsWith("/swagger") &&
            !path.startsWith("/v3/")
        ) return true

        if (PUBLIC_PREFIXES.any { path.startsWith(it) }) return true

        val method = request.method
        if (method == "GET" && PUBLIC_GET_PREFIXES.any { path.startsWith(it) }) return true

        return false
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)

        // No token supplied — proceed unauthenticated (downstream rules will block if needed)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.removePrefix(BEARER_PREFIX).trim()

        if (jwt.isBlank()) {
            rejectWith(response, "Invalid token format")
            return
        }

        try {
            // Blacklist check before any DB call
            if (tokenBlacklist.isBlacklisted(jwt)) {
                log.warn("SEC_EVENT=BLACKLISTED_TOKEN ip={} uri={}", clientIp(request), request.requestURI)
                rejectWith(response, "Token has been revoked")
                return
            }

            val username = jwtService.extractUsername(jwt)

            // Skip if already authenticated in this request context
            if (SecurityContextHolder.getContext().authentication != null) {
                filterChain.doFilter(request, response)
                return
            }

            val userDetails = loadWithCache(username)

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("SEC_EVENT=INVALID_TOKEN user={} ip={}", username, clientIp(request))
                rejectWith(response, "Invalid or expired token")
                return
            }

            // Guard: account still usable (catches disabled/locked/expired accounts)
            if (!userDetails.isEnabled || !userDetails.isAccountNonLocked ||
                !userDetails.isAccountNonExpired || !userDetails.isCredentialsNonExpired
            ) {
                userDetailsCache.invalidate(username)
                log.warn("SEC_EVENT=ACCOUNT_SUSPENDED user={} ip={}", username, clientIp(request))
                rejectWith(response, "Account is suspended or locked")
                return
            }

            val authToken = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            ).also { it.details = WebAuthenticationDetailsSource().buildDetails(request) }

            SecurityContextHolder.getContext().authentication = authToken
            log.debug("SEC_EVENT=AUTHN_OK user={} roles={}", username, userDetails.authorities)

        } catch (_: ExpiredJwtException) {
            log.info("SEC_EVENT=TOKEN_EXPIRED ip={}", clientIp(request))
            rejectWith(response, "Token expired — please login again")
            return
        } catch (_: MalformedJwtException) {
            log.warn("SEC_EVENT=MALFORMED_TOKEN ip={}", clientIp(request))
            rejectWith(response, "Malformed token")
            return
        } catch (_: SignatureException) {
            log.error("SEC_EVENT=INVALID_SIGNATURE ip={}", clientIp(request))
            rejectWith(response, "Token signature verification failed")
            return
        } catch (e: Exception) {
            log.error("SEC_EVENT=AUTHN_ERROR ip={}", clientIp(request), e)
            rejectWith(response, "Authentication error")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun loadWithCache(username: String): UserDetails =
        userDetailsCache.get(username) {
            log.debug("USER_CACHE_MISS username={}", it)
            userDetailsService.loadUserByUsername(it)
        } ?: throw IllegalStateException("UserDetailsService returned null for: $username")

    fun evictUser(username: String) {
        userDetailsCache.invalidate(username)
        log.debug("USER_CACHE_EVICTED username={}", username)
    }

    private fun rejectWith(response: HttpServletResponse, message: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """{"success":false,"code":"UNAUTHORIZED","message":"$message","timestamp":"${Instant.now()}"}"""
        )
    }

    private fun clientIp(request: HttpServletRequest): String {
        val cfIp = request.getHeader("CF-Connecting-IP")
        val xff = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        return when {
            !cfIp.isNullOrBlank() -> cfIp.trim()
            !xff.isNullOrBlank() -> xff.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp.trim()
            else -> request.remoteAddr
        }
    }

}
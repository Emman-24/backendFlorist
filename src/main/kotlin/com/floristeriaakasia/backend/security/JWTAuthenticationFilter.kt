package com.floristeriaakasia.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver


@Component
class JWTAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val handlerExceptionResolver: HandlerExceptionResolver

    ): OncePerRequestFilter() {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val TOKEN_COOKIE_NAME = "accessToken"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        return path.startsWith("/api/auth/") ||
                path.startsWith("/uploads/") ||
                path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/api/products") ||
                path.startsWith("/api/categories") ||
                path.startsWith("/api/subcategories") ||
                path.startsWith("/api/tags") ||
                path.startsWith("/error") ||
                path == "/login" ||
                path == "/access-denied"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var jwt: String? = null
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)

        jwt = if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            authHeader.substring(BEARER_PREFIX.length)
        }else{
            request.cookies?.find { it.name == TOKEN_COOKIE_NAME }?.value
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return
        }

        try {
            val username = jwtService.extractUsername(jwt)

            val authentication: Authentication? = SecurityContextHolder.getContext().authentication

            if (username != null && authentication == null) {
                val userDetails = this.userDetailsService.loadUserByUsername(username)

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
            filterChain.doFilter(request, response);

        }catch (e:Exception){
            handlerExceptionResolver.resolveException(request, response, null, e);
            logger.error("Error validating JWT: ${e.message}")
        }

    }


}
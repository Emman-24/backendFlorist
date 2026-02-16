package com.floristeriaakasia.backend.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
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
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI

        return !path.startsWith("/api/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/health")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {

            val authHeader = request.getHeader(AUTHORIZATION_HEADER)

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response)
                return
            }

            val jwt = authHeader.substring(BEARER_PREFIX.length)
            val username = jwtService.extractUsername(jwt)


            if (SecurityContextHolder.getContext().authentication == null) {
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

        } catch (e: ExpiredJwtException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired")
        }catch (e: MalformedJwtException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
        }catch (e: Exception) {
            logger.error("JWT validation error", e)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed")
        }

    }


}
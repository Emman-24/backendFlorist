package com.floristeriaakasia.backend.config

import com.floristeriaakasia.backend.security.JWTAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.time.Instant

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthFilter: JWTAuthenticationFilter,
    private val userDetailsService: UserDetailsService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/products/**",
                        "/api/categories/**",
                        "/api/subcategories/**",
                        "/api/tags/**",
                        "/api/faqs/**",
                        "/actuator/health"
                    ).permitAll()

                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                    .anyRequest().authenticated()

            }

            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, authException ->
                        response.contentType = "application/json"
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write(
                            """
                            {
                                "error": "Unauthorized",
                                "message": "${authException.message}",
                                "timestamp": "${Instant.now()}",
                                "path": "${request.requestURI}"
                            }
                        """.trimIndent()
                        )
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        response.contentType = "application/json"
                        response.status = HttpServletResponse.SC_FORBIDDEN
                        response.writer.write("""
                            {
                                "error": "Forbidden",
                                "message": "${accessDeniedException.message}",
                                "timestamp": "${Instant.now()}",
                                "path": "${request.requestURI}"
                            }
                        """.trimIndent())
                    }
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {

        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:4200",
            "https://www.floristeriaakasia.com.co"
        )

        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        )

        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("Authorization", "X-Total-Count")

        configuration.allowCredentials = true

        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", configuration)
        return source

    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }


    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

}
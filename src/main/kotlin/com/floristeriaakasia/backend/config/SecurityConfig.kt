package com.floristeriaakasia.backend.config

import com.floristeriaakasia.backend.security.JWTAuthenticationFilter
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

            .formLogin { formLogin ->
                formLogin.loginPage("/login")
                    .permitAll()
            }
            .httpBasic { it.disable() }

            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/logout").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subcategories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/tags/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/faqs/**").permitAll()

                    .requestMatchers("/uploads/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                    .requestMatchers("/admin/**").hasRole("ADMIN")

                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                    .anyRequest().authenticated()

            }

            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, authException ->
                        if (request.requestURI.startsWith("/api/")) {
                            response.contentType = "application/json"
                            response.status = 401
                            response.writer.write("""{"error": "Unauthorized", "message": "${authException.message}"}""")
                        } else {
                            response.sendRedirect("/login")
                        }
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        if (request.requestURI.startsWith("/api/")) {
                            response.contentType = "application/json"
                            response.status = 403
                            response.writer.write("""{"error": "Forbidden", "message": "${accessDeniedException.message}"}""")
                        } else {
                            response.sendRedirect("/access-denied")
                        }
                    }
            }

            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            .authenticationProvider(authenticationProvider())

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)


        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "https://www.floristeriaakasia.com.co",
            "http://localhost:4200",
            "http://localhost:3000"
        )

        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        )

        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept"
        )

        configuration.exposedHeaders = listOf(
            "Authorization",
            "X-Total-Count"
        )

        configuration.allowCredentials = true

        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
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
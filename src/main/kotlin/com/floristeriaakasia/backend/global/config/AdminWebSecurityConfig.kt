package com.floristeriaakasia.backend.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

@Configuration
class AdminWebSecurityConfig {

    @Bean
    @Order(2)
    fun adminWebFilterChain(http: HttpSecurity): SecurityFilterChain {

        val sessionRepo = HttpSessionSecurityContextRepository()

        http
            .securityMatcher("/admin/**")
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/admin/login")
            }

            .sessionManagement { session ->
                session
                    .sessionFixation().migrateSession()
                    .maximumSessions(1)
                    .expiredUrl("/admin/login?expired")
            }

            .securityContext { ctx ->
                ctx.securityContextRepository(sessionRepo)
            }

            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/admin/login").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
            }

            .formLogin { form ->
                form
                    .loginPage("/admin/login")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .failureUrl("/admin/login?error")
                    .permitAll()
            }

            .logout { logout ->
                logout
                    .logoutUrl("/admin/logout-form")
                    .logoutSuccessUrl("/admin/login?logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            }

            .requestCache { cache ->
                cache.requestCache(HttpSessionRequestCache())
            }

            .exceptionHandling { ex ->
                ex.accessDeniedPage("/admin/login?denied")
            }

        return http.build()
    }
}
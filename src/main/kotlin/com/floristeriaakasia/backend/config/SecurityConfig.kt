package com.floristeriaakasia.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import javax.sql.DataSource

@Configuration
class SecurityConfig {

    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { auto ->
                auto
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/**").hasRole("USER")
                    .anyRequest().authenticated()
            }
            .formLogin { }
            .logout { }
            .httpBasic { }
            .build()
    }


    @Bean
    fun users(dataSource: DataSource): UserDetailsService {
        return JdbcUserDetailsManager(dataSource)
    }
}
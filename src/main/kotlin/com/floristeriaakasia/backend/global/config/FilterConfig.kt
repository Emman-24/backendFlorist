package com.floristeriaakasia.backend.global.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
class FilterConfig {

    @Bean
    fun rateLimitFilterRegistration(rateLimitFilter: RateLimitFilter): FilterRegistrationBean<RateLimitFilter> {
        val registration = FilterRegistrationBean<RateLimitFilter>()
        registration.filter = rateLimitFilter
        registration.addUrlPatterns("/api/*")
        registration.order = Ordered.HIGHEST_PRECEDENCE + 1
        registration.setName("rateLimitFilter")
        return registration
    }
}


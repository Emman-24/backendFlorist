package com.floristeriaakasia.backend.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:4200",
                "https://www.floristeriaakasia.com.co"
            )
            .allowedMethods("GET","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

}
package com.floristeriaakasia.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("public-api")
            .packagesToScan("com.floristeriaakasia.backend.controller.api")
            .pathsToMatch("/api/**")
            .build()
    }

    @Bean
    fun apiInfo(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Floristería Akasia API")
                    .description("REST API for Floristería Akasia backend application")
                    .version("1.0.0")
            )
    }
}
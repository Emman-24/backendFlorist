package com.floristeriaakasia.backend.config

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MySQLContainer


object TestMySqlContainer {

    val instance: MySQLContainer<*> = MySQLContainer("mysql:8.0.23")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test")
        .apply { start() }

    fun registerDataSource(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", instance::getJdbcUrl)
        registry.add("spring.datasource.username", instance::getUsername)
        registry.add("spring.datasource.password", instance::getPassword)
    }

    fun registerApplicationProperties(registry: DynamicPropertyRegistry) {
        registry.add("security.jwt.secret-key") { "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" }
        registry.add("security.jwt.expiration-time") { "3600000" }
        registry.add("security.refresh-expiration-ms") { "86400000" }
        registry.add("cloudinary.cloud-name") { "test" }
        registry.add("cloudinary.api-key") { "test" }
        registry.add("cloudinary.api-secret") { "test" }
        registry.add("app.security.environment") { "test" }
        registry.add("app.cors.allowed-origins") { "http://localhost:4200" }
        registry.add("JPA_SHOW_SQL") { "true" }
        registry.add("LOG_LEVEL_ROOT") { "INFO" }
        registry.add("LOG_LEVEL_APP") { "INFO" }
        registry.add("LOG_LEVEL_SQL") { "INFO" }
        registry.add("LOG_LEVEL_SQL_BINDER") { "INFO" }
    }
}

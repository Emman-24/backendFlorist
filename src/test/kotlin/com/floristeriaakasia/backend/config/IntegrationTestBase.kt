package com.floristeriaakasia.backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    companion object {
        @Container
        @JvmStatic
        val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0.23")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry : DynamicPropertyRegistry){
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("security.jwt.secret-key") { "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970" }
            registry.add("security.jwt.expiration-time") { "3600000" }
            registry.add("security.refresh-expiration-ms") { "86400000" }
            registry.add("cloudinary.cloud-name") { "test" }
            registry.add("cloudinary.api-key") { "test" }
            registry.add("cloudinary.api-secret") { "test" }
            registry.add("JPA_SHOW_SQL") { "true" }
            registry.add("LOG_LEVEL_ROOT") { "INFO" }
            registry.add("LOG_LEVEL_APP") { "INFO" }
            registry.add("LOG_LEVEL_SQL") { "INFO" }
            registry.add("LOG_LEVEL_SQL_BINDER") { "INFO" }
        }
    }

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var testRestTemplate: TestRestTemplate

    protected fun getBaseUrl() = "http://localhost:$port"
}
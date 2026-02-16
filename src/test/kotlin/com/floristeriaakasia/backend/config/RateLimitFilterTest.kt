package com.floristeriaakasia.backend.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class RateLimitFilterTest : IntegrationTestBase() {

    @Test
    fun `should include X-RateLimit-Reset header in response`() {
        val response: ResponseEntity<String> = testRestTemplate.getForEntity("/api/products", String::class.java)

        val resetHeader = response.headers["X-RateLimit-Reset"]
        assertNotNull(resetHeader, "X-RateLimit-Reset header should be present")
        val resetValue = resetHeader?.firstOrNull()?.toLong()
        assertNotNull(resetValue, "X-RateLimit-Reset value should be a valid long")
        println("[DEBUG_LOG] X-RateLimit-Reset: $resetValue")
    }
}

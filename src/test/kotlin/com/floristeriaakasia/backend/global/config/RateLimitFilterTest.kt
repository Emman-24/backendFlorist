package com.floristeriaakasia.backend.global.config

import com.floristeriaakasia.backend.config.IntegrationTestBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.ResponseEntity

class RateLimitFilterTest: IntegrationTestBase() {

    @Test
    fun `should include X-RateLimit-Reset header on floral arrangement list endpoint`() {
        val response: ResponseEntity<String> = testRestTemplate.getForEntity<String>("/api/floral-arrangement")

        val resetHeader = response.headers["X-RateLimit-Reset"]
        assertNotNull(resetHeader, "X-RateLimit-Reset header should be present")

        val resetValue = resetHeader?.firstOrNull()?.toLong()
        assertNotNull(resetValue, "X-RateLimit-Reset value should be a valid long")

        println("[DEBUG_LOG] X-RateLimit-Reset: $resetValue")
    }

    @Test
    fun `should include X-RateLimit-Reset header on faq endpoint`() {
        val response: ResponseEntity<String> = testRestTemplate.getForEntity<String>("/api/faqs")

        val resetHeader = response.headers["X-RateLimit-Reset"]
        assertNotNull(resetHeader, "X-RateLimit-Reset header should be present on FAQ endpoint")
    }
}
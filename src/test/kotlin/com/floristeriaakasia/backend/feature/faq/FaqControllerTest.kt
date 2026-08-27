package com.floristeriaakasia.backend.feature.faq

import com.floristeriaakasia.backend.config.IntegrationTestBase
import com.floristeriaakasia.backend.util.ApiResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class FaqControllerTest : IntegrationTestBase() {

    @Test
    fun `GET api-faqs returns 200 with empty list when no faqs exist`() {
        val response = testRestTemplate.exchange(
            "${getBaseUrl()}/api/faqs",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<ApiResponse.Success<List<FaqResponse>>>() {}
        )
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `POST api-faqs returns 401 when called without authentication`() {
        val request = CreateFaqRequest(
            question = "What are your opening hours?",
            answer   = "We are open Monday to Saturday from 9am to 7pm.",
            position = 0,
            status   = true
        )
        val response = testRestTemplate.postForEntity<String>(
            "${getBaseUrl()}/api/faqs",
            HttpEntity(request)
        )
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }


    @Test
    fun `GET api-faqs-id returns 404 for non-existent FAQ`() {
        val response = testRestTemplate.getForEntity<String>(
            "${getBaseUrl()}/api/faqs/99999"
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

}
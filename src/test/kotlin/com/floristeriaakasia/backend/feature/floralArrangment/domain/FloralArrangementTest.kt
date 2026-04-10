package com.floristeriaakasia.backend.feature.floralArrangment.domain

import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FloralArrangementTest {

    @Test
    fun `should not throw StackOverflowError when adding image to gallery`() {
        val floral = FloralArrangement(
            id = 1L,
            name = "Test Arrangement",
            seoName = "test-arrangement",
            slug = "test-arrangement",
            price = Price(price = BigDecimal.TEN, currency = "USD"),
            isAvailable = true,
            seasonal = false,
            featured = false
        )

        val image = ProductGallery(
            id = 1L,
            publicId = "test-public-id",
            originalUrl = "test-original-url",
            floralArrangement = floral,
            thumbnailUrl = "test-thumbnail-url",
            mediumUrl = "test-medium-url",
            altText = "test-alt-text",
            position = 0
        )

        assertDoesNotThrow {
            floral.addImage(image)
            floral.hashCode()
            floral.toString()
        }
    }
}

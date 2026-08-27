package com.floristeriaakasia.backend.feature.floralArrangment.domain

import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementCommand
import com.floristeriaakasia.backend.feature.price.Price
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class FloralArrangementDomainTest {

    @Nested
    @DisplayName("FloralArrangement Entity Tests")
    inner class FloralArrangementEntityTests {

        private fun createArrangement(
            id: Long = 0L,
            name: String = "Bouquet Primavera",
            slug: String = "bouquet-primavera",
            price: BigDecimal = BigDecimal("50000.00")
        ) = FloralArrangement(
            id = id,
            name = name,
            seoName = slug,
            slug = slug,
            price = Price(price = price),
            isAvailable = true,
            seasonal = false,
            featured = true
        )

        @Test
        fun `addImage should set isPrimary to true when gallery is empty`() {
            val arrangement = createArrangement()
            val image = ProductGallery(
                id = 0L,
                publicId = "img-1",
                originalUrl = "https://example.com/img1.jpg",
                thumbnailUrl = "https://example.com/thumb1.jpg",
                mediumUrl = "https://example.com/med1.jpg",
                altText = "Primary view",
                isPrimary = false,
                position = 0
            )

            arrangement.addImage(image)

            assertEquals(1, arrangement.gallery.size)
            assertTrue(image.isPrimary)
            assertTrue(arrangement.gallery.contains(image))
        }

        @Test
        fun `addImage should keep original isPrimary when gallery is not empty`() {
            val arrangement = createArrangement()
            val firstImage = ProductGallery(
                id = 1L,
                publicId = "img-1",
                originalUrl = "https://example.com/img1.jpg",
                thumbnailUrl = "https://example.com/thumb1.jpg",
                mediumUrl = "https://example.com/med1.jpg",
                altText = "First image",
                isPrimary = false,
                position = 0
            )
            val secondImage = ProductGallery(
                id = 2L,
                publicId = "img-2",
                originalUrl = "https://example.com/img2.jpg",
                thumbnailUrl = "https://example.com/thumb2.jpg",
                mediumUrl = "https://example.com/med2.jpg",
                altText = "Second image",
                isPrimary = false,
                position = 1
            )

            arrangement.addImage(firstImage)
            arrangement.addImage(secondImage)

            assertEquals(2, arrangement.gallery.size)
            assertTrue(firstImage.isPrimary)
            assertFalse(secondImage.isPrimary)
        }

        @Test
        fun `equals and hashCode based on id when id is non-zero`() {
            val arrangement1 = createArrangement(id = 10L, slug = "slug-a")
            val arrangement2 = createArrangement(id = 10L, slug = "slug-b")
            val arrangement3 = createArrangement(id = 20L, slug = "slug-a")

            assertEquals(arrangement1, arrangement2)
            assertEquals(arrangement1.hashCode(), arrangement2.hashCode())
            assertNotEquals(arrangement1, arrangement3)
            assertNotEquals(arrangement1.hashCode(), arrangement3.hashCode())
        }

        @Test
        fun `equals and hashCode based on slug when id is 0`() {
            val arrangement1 = createArrangement(id = 0L, slug = "bouquet-rosas")
            val arrangement2 = createArrangement(id = 0L, slug = "bouquet-rosas")
            val arrangement3 = createArrangement(id = 0L, slug = "bouquet-tulipanes")

            assertEquals(arrangement1, arrangement2)
            assertEquals(arrangement1.hashCode(), arrangement2.hashCode())
            assertNotEquals(arrangement1, arrangement3)
            assertNotEquals(arrangement1.hashCode(), arrangement3.hashCode())
        }

        @Test
        fun `equals returns true for same instance and false for null or other types`() {
            val arrangement = createArrangement(id = 1L, slug = "test")

            assertEquals(arrangement, arrangement)
            assertFalse(arrangement.equals(null))
            assertFalse(arrangement.equals("not an arrangement"))
        }

        @Test
        fun `toString formats correctly`() {
            val arrangement = createArrangement(id = 5L, name = "Rosas Rojas", slug = "rosas-rojas")
            assertEquals("FloralArrangement(id=5, name='Rosas Rojas', slug='rosas-rojas')", arrangement.toString())
        }
    }

    @Nested
    @DisplayName("ProductGallery Entity Tests")
    inner class ProductGalleryEntityTests {

        private fun createGalleryItem(
            id: Long = 0L,
            publicId: String = "pub-123",
            isPrimary: Boolean = false,
            position: Int = 0
        ) = ProductGallery(
            id = id,
            publicId = publicId,
            originalUrl = "https://example.com/orig.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg",
            mediumUrl = "https://example.com/med.jpg",
            altText = "Alt description",
            isPrimary = isPrimary,
            position = position
        )

        @Test
        fun `equals and hashCode based on id when id is non-zero`() {
            val item1 = createGalleryItem(id = 1L, publicId = "pub-1")
            val item2 = createGalleryItem(id = 1L, publicId = "pub-2")
            val item3 = createGalleryItem(id = 2L, publicId = "pub-1")

            assertEquals(item1, item2)
            assertEquals(item1.hashCode(), item2.hashCode())
            assertNotEquals(item1, item3)
            assertNotEquals(item1.hashCode(), item3.hashCode())
        }

        @Test
        fun `equals and hashCode based on publicId when id is 0`() {
            val item1 = createGalleryItem(id = 0L, publicId = "pub-same")
            val item2 = createGalleryItem(id = 0L, publicId = "pub-same")
            val item3 = createGalleryItem(id = 0L, publicId = "pub-diff")

            assertEquals(item1, item2)
            assertEquals(item1.hashCode(), item2.hashCode())
            assertNotEquals(item1, item3)
            assertNotEquals(item1.hashCode(), item3.hashCode())
        }

        @Test
        fun `equals returns true for same instance and false for null or other types`() {
            val item = createGalleryItem(id = 1L)

            assertEquals(item, item)
            assertFalse(item.equals(null))
            assertFalse(item.equals(123))
        }

        @Test
        fun `toString formats correctly`() {
            val item = createGalleryItem(id = 7L, publicId = "flower-img", isPrimary = true, position = 3)
            assertEquals("ProductGallery(id=7, publicId='flower-img', isPrimary=true, position=3)", item.toString())
        }
    }

    @Nested
    @DisplayName("CreateFloralArrangementCommand Validation Tests")
    inner class CreateFloralArrangementCommandValidationTests {

        private fun validCommandBuilder(
            name: String = "Arreglo Tropical",
            seoName: String = "arreglo-tropical",
            categoryIds: Set<Long> = setOf(1L, 2L),
            priceAmount: BigDecimal = BigDecimal("85000.00"),
            discountPriceAmount: BigDecimal? = null,
            currency: String = "COP",
            isAvailable: Boolean = true,
            seasonal: Boolean = false,
            featured: Boolean = true,
            description: String = "Descripción detallada del arreglo tropical.",
            shortDescription: String = "Descripción corta.",
            flowers: List<CreateFloralArrangementCommand.FlowerData> = listOf(
                CreateFloralArrangementCommand.FlowerData(name = "Anturio", meaning = "Hospitalidad")
            ),
            tagIds: Set<Long> = setOf(10L)
        ) = CreateFloralArrangementCommand(
            name = name,
            seoName = seoName,
            categoryIds = categoryIds,
            priceAmount = priceAmount,
            discountPriceAmount = discountPriceAmount,
            currency = currency,
            isAvailable = isAvailable,
            seasonal = seasonal,
            featured = featured,
            description = description,
            shortDescription = shortDescription,
            flowers = flowers,
            tagIds = tagIds
        )

        @Test
        fun `should create command successfully with valid data`() {
            val cmd = validCommandBuilder()

            assertEquals("Arreglo Tropical", cmd.name)
            assertEquals("arreglo-tropical", cmd.seoName)
            assertEquals(setOf(1L, 2L), cmd.categoryIds)
            assertEquals(BigDecimal("85000.00"), cmd.priceAmount)
            assertNull(cmd.discountPriceAmount)
            assertEquals("COP", cmd.currency)
            assertTrue(cmd.isAvailable)
            assertFalse(cmd.seasonal)
            assertTrue(cmd.featured)
            assertEquals("Descripción corta.", cmd.shortDescription)
            assertEquals("Descripción detallada del arreglo tropical.", cmd.description)
            assertEquals(1, cmd.flowers.size)
            assertEquals(setOf(10L), cmd.tagIds)
        }

        @Test
        fun `should create command successfully with valid discount price`() {
            val cmd = validCommandBuilder(
                priceAmount = BigDecimal("100000.00"),
                discountPriceAmount = BigDecimal("80000.00")
            )

            assertEquals(BigDecimal("100000.00"), cmd.priceAmount)
            assertEquals(BigDecimal("80000.00"), cmd.discountPriceAmount)
        }

        @Test
        fun `should throw IllegalArgumentException when name is blank`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(name = "   ")
            }
            assertEquals("Arrangement name must not be blank", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when categoryIds is empty`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(categoryIds = emptySet())
            }
            assertEquals("At least one category must be provided", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when priceAmount is zero`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(priceAmount = BigDecimal.ZERO)
            }
            assertEquals("Price must be greater than zero", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when priceAmount is negative`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(priceAmount = BigDecimal("-1000.00"))
            }
            assertEquals("Price must be greater than zero", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when currency is not 3 characters`() {
            val exShort = assertThrows<IllegalArgumentException> {
                validCommandBuilder(currency = "US")
            }
            assertEquals("Currency must be an ISO-4217 code (e.g. COP, USD)", exShort.message)

            val exLong = assertThrows<IllegalArgumentException> {
                validCommandBuilder(currency = "USDD")
            }
            assertEquals("Currency must be an ISO-4217 code (e.g. COP, USD)", exLong.message)
        }

        @Test
        fun `should throw IllegalArgumentException when discount price is equal to priceAmount`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(
                    priceAmount = BigDecimal("50000.00"),
                    discountPriceAmount = BigDecimal("50000.00")
                )
            }
            assertEquals("Discount price must be less than the original price", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when discount price is greater than priceAmount`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(
                    priceAmount = BigDecimal("50000.00"),
                    discountPriceAmount = BigDecimal("60000.00")
                )
            }
            assertEquals("Discount price must be less than the original price", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when shortDescription is blank`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(shortDescription = "   ")
            }
            assertEquals("shortDescription is required when description is provided", ex.message)
        }

        @Test
        fun `should throw IllegalArgumentException when description is blank`() {
            val ex = assertThrows<IllegalArgumentException> {
                validCommandBuilder(description = "   ")
            }
            assertEquals("description is required when shortDescription is provided", ex.message)
        }

        @Test
        fun `FlowerData should throw IllegalArgumentException when name is blank`() {
            val ex = assertThrows<IllegalArgumentException> {
                CreateFloralArrangementCommand.FlowerData(name = "   ", meaning = "Amor eterno")
            }
            assertEquals("Flower name must not be blank", ex.message)
        }

        @Test
        fun `FlowerData should throw IllegalArgumentException when meaning is blank`() {
            val ex = assertThrows<IllegalArgumentException> {
                CreateFloralArrangementCommand.FlowerData(name = "Rosa Roja", meaning = "   ")
            }
            assertEquals("Flower meaning must not be blank", ex.message)
        }
    }
}

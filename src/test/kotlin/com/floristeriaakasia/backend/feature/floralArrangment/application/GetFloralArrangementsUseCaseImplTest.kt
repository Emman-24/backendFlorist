package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.category.Category
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.*
import com.floristeriaakasia.backend.feature.flowers.Flowers
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import com.floristeriaakasia.backend.feature.tag.Tag
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import com.floristeriaakasia.backend.global.exeption.FloralArrangementSeoNameNotFoundException
import com.floristeriaakasia.backend.global.exeption.FloralArrangementSlugNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.Instant

class GetFloralArrangementsUseCaseImplTest {

    private lateinit var saveFloralArrangementPort: SaveFloralArrangementPort
    private lateinit var useCase: GetFloralArrangementsUseCaseImpl

    @BeforeEach
    fun setUp() {
        saveFloralArrangementPort = mock(SaveFloralArrangementPort::class.java)
        useCase = GetFloralArrangementsUseCaseImpl(saveFloralArrangementPort)
    }

    private fun createSampleArrangement(
        id: Long,
        name: String = "Bouquet Rosas",
        slug: String = "bouquet-rosas",
        seoName: String = "bouquet-rosas",
        price: BigDecimal = BigDecimal("100000.00"),
        discountPrice: BigDecimal? = BigDecimal("80000.00"),
        currency: String = "COP",
        isAvailable: Boolean = true,
        seasonal: Boolean = false,
        featured: Boolean = true,
        views: Int = 10
    ): FloralArrangement {
        val arrangement = FloralArrangement(
            id = id,
            name = name,
            seoName = seoName,
            slug = slug,
            price = Price(
                id = id,
                price = price,
                discountPrice = discountPrice,
                currency = currency,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            ),
            isAvailable = isAvailable,
            seasonal = seasonal,
            featured = featured,
            views = views,
            description = ProductDescription(
                id = id,
                shortDescription = "Short desc for $name",
                description = "Long detailed desc for $name",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        return arrangement
    }

    @Nested
    @DisplayName("execute(query, pageable) Tests")
    inner class ExecuteQueryTests {

        @Test
        fun `execute should return mapped paged summaries when results exist`() {
            val pageable: Pageable = PageRequest.of(0, 10)
            val query = FloralArrangementQuery(
                categoryId = 1L,
                tagId = 2L,
                featured = true,
                seasonal = false,
                isAvailable = true,
                minPrice = BigDecimal("50000.00"),
                maxPrice = BigDecimal("150000.00")
            )

            val fa1 = createSampleArrangement(id = 1L, name = "Rosas Rojas", price = BigDecimal("100000.00"), discountPrice = BigDecimal("80000.00"))
            val fa2 = createSampleArrangement(id = 2L, name = "Girasoles", price = BigDecimal("50000.00"), discountPrice = null)

            val page = PageImpl(listOf(fa1, fa2), pageable, 2)
            `when`(saveFloralArrangementPort.findAllWithFilters(
                categoryId = query.categoryId,
                tagId = query.tagId,
                featured = query.featured,
                seasonal = query.seasonal,
                isAvailable = query.isAvailable,
                minPrice = query.minPrice,
                maxPrice = query.maxPrice,
                pageable = pageable
            )).thenReturn(page)

            // Mock categories enrichment
            val cat1 = Category(id = 1L, name = "Romance", slug = "romance", path = "/1/", depth = 0)
            val fa1WithCats = createSampleArrangement(id = 1L).apply { categories.add(cat1) }
            val fa2WithCats = createSampleArrangement(id = 2L)
            `when`(saveFloralArrangementPort.findWithCategoriesByIds(listOf(1L, 2L)))
                .thenReturn(listOf(fa1WithCats, fa2WithCats))

            // Mock tags enrichment
            val tag1 = Tag(id = 10L, text = "Rosas", route = "rosas", description = "Tag rosas")
            val fa1WithTags = createSampleArrangement(id = 1L).apply { tags.add(tag1) }
            val fa2WithTags = createSampleArrangement(id = 2L)
            `when`(saveFloralArrangementPort.findWithTagsByIds(listOf(1L, 2L)))
                .thenReturn(listOf(fa1WithTags, fa2WithTags))

            // Mock primary images enrichment
            val primaryImg1 = ProductGallery(
                id = 101L,
                publicId = "img-fa1",
                originalUrl = "https://example.com/orig1.jpg",
                thumbnailUrl = "https://example.com/thumb1.jpg",
                mediumUrl = "https://example.com/med1.jpg",
                altText = "Rosas Rojas Main",
                isPrimary = true,
                position = 0,
                floralArrangement = fa1
            )
            `when`(saveFloralArrangementPort.findPrimaryImagesByArrangementIds(listOf(1L, 2L)))
                .thenReturn(listOf(primaryImg1))

            val resultPage = useCase.execute(query, pageable)

            assertEquals(2, resultPage.totalElements)
            assertEquals(2, resultPage.content.size)

            // Check first item (with discount, category, tag, and primary image)
            val item1 = resultPage.content[0]
            assertEquals(1L, item1.id)
            assertEquals("Rosas Rojas", item1.name)
            assertTrue(item1.price.hasDiscount)
            assertEquals(20, item1.price.discountPercent) // (100000 - 80000) / 100000 * 100 = 20%
            assertNotNull(item1.primaryImage)
            assertEquals("img-fa1", item1.primaryImage?.publicId)
            assertEquals("https://example.com/med1.jpg", item1.primaryImage?.mediumUrl)
            assertEquals("Rosas Rojas Main", item1.primaryImage?.altText)
            assertEquals(1, item1.categories.size)
            assertEquals("Romance", item1.categories.first().name)
            assertEquals(1, item1.tags.size)
            assertEquals("Rosas", item1.tags.first().text)

            // Check second item (no discount, no category, no tag, no primary image)
            val item2 = resultPage.content[1]
            assertEquals(2L, item2.id)
            assertEquals("Girasoles", item2.name)
            assertFalse(item2.price.hasDiscount)
            assertNull(item2.price.discountPercent)
            assertNull(item2.primaryImage)
            assertTrue(item2.categories.isEmpty())
            assertTrue(item2.tags.isEmpty())
        }

        @Test
        fun `execute should return empty page without extra queries when no arrangements match`() {
            val pageable: Pageable = PageRequest.of(0, 10)
            val query = FloralArrangementQuery()

            `when`(saveFloralArrangementPort.findAllWithFilters(
                categoryId = null,
                tagId = null,
                featured = null,
                seasonal = null,
                isAvailable = null,
                minPrice = null,
                maxPrice = null,
                pageable = pageable
            )).thenReturn(PageImpl(emptyList(), pageable, 0))

            val resultPage = useCase.execute(query, pageable)

            assertTrue(resultPage.isEmpty)
            assertEquals(0, resultPage.totalElements)
            verify(saveFloralArrangementPort, never()).findWithCategoriesByIds(anyList())
            verify(saveFloralArrangementPort, never()).findWithTagsByIds(anyList())
            verify(saveFloralArrangementPort, never()).findPrimaryImagesByArrangementIds(anyList())
        }
    }

    @Nested
    @DisplayName("executeById(id) Tests")
    inner class ExecuteByIdTests {

        @Test
        fun `executeById should return detail DTO and increment views when arrangement exists`() {
            val fa = createSampleArrangement(id = 5L, name = "Lirios Elegantes", price = BigDecimal("90000.00"), discountPrice = BigDecimal("72000.00"))
            val cat = Category(id = 1L, name = "Elegante", slug = "elegante", path = "/1/", depth = 0)
            val tag = Tag(id = 2L, text = "Lirios", route = "lirios", description = "Tag Lirios")
            val flower = Flowers(id = 10L, name = "Lirio", meaning = "Pureza", floralArrangement = fa)

            val galleryImg1 = ProductGallery(
                id = 1L,
                publicId = "img-2",
                originalUrl = "https://example.com/img2.jpg",
                thumbnailUrl = "https://example.com/thumb2.jpg",
                mediumUrl = "https://example.com/med2.jpg",
                altText = "Second",
                position = 2,
                isPrimary = false,
                floralArrangement = fa
            )
            val galleryImg2 = ProductGallery(
                id = 2L,
                publicId = "img-1",
                originalUrl = "https://example.com/img1.jpg",
                thumbnailUrl = "https://example.com/thumb1.jpg",
                mediumUrl = "https://example.com/med1.jpg",
                altText = "First",
                position = 1,
                isPrimary = true,
                floralArrangement = fa
            )

            fa.categories.add(cat)
            fa.tags.add(tag)
            fa.flowers.add(flower)
            fa.gallery.add(galleryImg1)
            fa.gallery.add(galleryImg2)

            `when`(saveFloralArrangementPort.findDetails(5L)).thenReturn(fa)

            val detail = useCase.executeById(5L)

            verify(saveFloralArrangementPort).incrementViews(5L)
            assertEquals(5L, detail.id)
            assertEquals("Lirios Elegantes", detail.name)
            assertEquals("Short desc for Lirios Elegantes", detail.description?.shortDescription)
            assertEquals("Long detailed desc for Lirios Elegantes", detail.description?.description)
            assertTrue(detail.price.hasDiscount)
            assertEquals(20, detail.price.discountPercent)
            assertEquals(1, detail.categories.size)
            assertEquals("Elegante", detail.categories.first().name)
            assertEquals(1, detail.tags.size)
            assertEquals("Lirios", detail.tags.first().text)
            assertEquals(1, detail.flowers.size)
            assertEquals("Lirio", detail.flowers.first().name)

            // Gallery should be sorted by position (position 1 comes before position 2)
            assertEquals(2, detail.gallery.size)
            assertEquals(1, detail.gallery[0].position)
            assertEquals("img-1", detail.gallery[0].publicId)
            assertEquals(2, detail.gallery[1].position)
            assertEquals("img-2", detail.gallery[1].publicId)
        }

        @Test
        fun `executeById should throw FloralArrangementNotFoundException when not found`() {
            `when`(saveFloralArrangementPort.findDetails(999L)).thenReturn(null)

            val ex = assertThrows<FloralArrangementNotFoundException> {
                useCase.executeById(999L)
            }
            assertTrue(ex.message!!.contains("999"))
            verify(saveFloralArrangementPort, never()).incrementViews(anyLong())
        }
    }

    @Nested
    @DisplayName("executeBySlug(slug) Tests")
    inner class ExecuteBySlugTests {

        @Test
        fun `executeBySlug should return detail DTO and increment views when arrangement exists`() {
            val fa = createSampleArrangement(id = 8L, slug = "orquidea-imperial")
            `when`(saveFloralArrangementPort.findBySlug("orquidea-imperial")).thenReturn(fa)

            val result = useCase.executeBySlug("orquidea-imperial")

            assertEquals(8L, result.id)
            assertEquals("orquidea-imperial", result.slug)
            verify(saveFloralArrangementPort).incrementViews(8L)
        }

        @Test
        fun `executeBySlug should throw FloralArrangementSlugNotFoundException when not found`() {
            `when`(saveFloralArrangementPort.findBySlug("unknown-slug")).thenReturn(null)

            val ex = assertThrows<FloralArrangementSlugNotFoundException> {
                useCase.executeBySlug("unknown-slug")
            }
            assertTrue(ex.message!!.contains("unknown-slug"))
            verify(saveFloralArrangementPort, never()).incrementViews(anyLong())
        }
    }

    @Nested
    @DisplayName("executeBySeoName(seoName) Tests")
    inner class ExecuteBySeoNameTests {

        @Test
        fun `executeBySeoName should return detail DTO and increment views when arrangement exists`() {
            val fa = createSampleArrangement(id = 12L, seoName = "tulipanes-holandeses")
            `when`(saveFloralArrangementPort.findBySeoName("tulipanes-holandeses")).thenReturn(fa)

            val result = useCase.executeBySeoName("tulipanes-holandeses")

            assertEquals(12L, result.id)
            assertEquals("tulipanes-holandeses", result.seoName)
            verify(saveFloralArrangementPort).incrementViews(12L)
        }

        @Test
        fun `executeBySeoName should throw FloralArrangementSeoNameNotFoundException when not found`() {
            `when`(saveFloralArrangementPort.findBySeoName("unknown-seo")).thenReturn(null)

            val ex = assertThrows<FloralArrangementSeoNameNotFoundException> {
                useCase.executeBySeoName("unknown-seo")
            }
            assertTrue(ex.message!!.contains("unknown-seo"))
            verify(saveFloralArrangementPort, never()).incrementViews(anyLong())
        }
    }
}

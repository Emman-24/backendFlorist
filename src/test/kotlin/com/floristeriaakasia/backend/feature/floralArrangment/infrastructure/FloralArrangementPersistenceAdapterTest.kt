package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import com.floristeriaakasia.backend.feature.price.Price
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.util.*

class FloralArrangementPersistenceAdapterTest {

    private lateinit var floralArrangementRepo: FloralArrangementRepository
    private lateinit var adapter: FloralArrangementPersistenceAdapter

    @BeforeEach
    fun setUp() {
        floralArrangementRepo = mock(FloralArrangementRepository::class.java)
        adapter = FloralArrangementPersistenceAdapter(floralArrangementRepo)
    }

    private fun createSampleArrangement(id: Long = 1L, slug: String = "sample-arrangement") = FloralArrangement(
        id = id,
        name = "Sample Arrangement",
        seoName = slug,
        slug = slug,
        price = Price(price = BigDecimal("60000.00"), currency = "COP"),
        isAvailable = true,
        seasonal = false,
        featured = true
    )

    @Test
    fun `save should delegate to repository and return saved entity id`() {
        val arrangement = createSampleArrangement(id = 0L)
        val savedArrangement = createSampleArrangement(id = 55L)

        `when`(floralArrangementRepo.save(arrangement)).thenReturn(savedArrangement)

        val id = adapter.save(arrangement)

        assertEquals(55L, id)
        verify(floralArrangementRepo).save(arrangement)
    }

    @Test
    fun `existsBySlug should delegate to repository`() {
        `when`(floralArrangementRepo.existsBySlug("existing-slug")).thenReturn(true)
        `when`(floralArrangementRepo.existsBySlug("missing-slug")).thenReturn(false)

        assertTrue(adapter.existsBySlug("existing-slug"))
        assertFalse(adapter.existsBySlug("missing-slug"))

        verify(floralArrangementRepo).existsBySlug("existing-slug")
        verify(floralArrangementRepo).existsBySlug("missing-slug")
    }

    @Test
    fun `findById should return entity when present in repository`() {
        val arrangement = createSampleArrangement(id = 10L)
        `when`(floralArrangementRepo.findById(10L)).thenReturn(Optional.of(arrangement))

        val result = adapter.findById(10L)

        assertNotNull(result)
        assertEquals(10L, result?.id)
        verify(floralArrangementRepo).findById(10L)
    }

    @Test
    fun `findById should return null when absent in repository`() {
        `when`(floralArrangementRepo.findById(99L)).thenReturn(Optional.empty())

        val result = adapter.findById(99L)

        assertNull(result)
        verify(floralArrangementRepo).findById(99L)
    }

    @Test
    fun `incrementViews should delegate to repository`() {
        doNothing().`when`(floralArrangementRepo).incrementViews(15L)

        adapter.incrementViews(15L)

        verify(floralArrangementRepo).incrementViews(15L)
    }

    @Test
    fun `findDetails should delegate to repository findByIdWithDetails`() {
        val arrangement = createSampleArrangement(id = 20L)
        `when`(floralArrangementRepo.findByIdWithDetails(20L)).thenReturn(arrangement)

        val result = adapter.findDetails(20L)

        assertNotNull(result)
        assertEquals(20L, result?.id)
        verify(floralArrangementRepo).findByIdWithDetails(20L)
    }

    @Test
    fun `findAllWithFilters should delegate to repository with all filter parameters`() {
        val pageable = PageRequest.of(0, 10)
        val arrangement = createSampleArrangement(id = 1L)
        val expectedPage = PageImpl(listOf(arrangement), pageable, 1)

        `when`(floralArrangementRepo.findAllWithFilters(
            categoryId = 1L,
            tagId = 2L,
            featured = true,
            seasonal = false,
            isAvailable = true,
            minPrice = BigDecimal("10000.00"),
            maxPrice = BigDecimal("90000.00"),
            pageable = pageable
        )).thenReturn(expectedPage)

        val resultPage = adapter.findAllWithFilters(
            categoryId = 1L,
            tagId = 2L,
            featured = true,
            seasonal = false,
            isAvailable = true,
            minPrice = BigDecimal("10000.00"),
            maxPrice = BigDecimal("90000.00"),
            pageable = pageable
        )

        assertEquals(1, resultPage.totalElements)
        assertEquals(arrangement, resultPage.content.first())
        verify(floralArrangementRepo).findAllWithFilters(
            1L, 2L, true, false, true, BigDecimal("10000.00"), BigDecimal("90000.00"), pageable
        )
    }

    @Test
    fun `findBySlug should delegate to repository`() {
        val arrangement = createSampleArrangement(slug = "tulipanes-rojos")
        `when`(floralArrangementRepo.findBySlug("tulipanes-rojos")).thenReturn(arrangement)

        val result = adapter.findBySlug("tulipanes-rojos")

        assertNotNull(result)
        assertEquals("tulipanes-rojos", result?.slug)
        verify(floralArrangementRepo).findBySlug("tulipanes-rojos")
    }

    @Test
    fun `findBySeoName should delegate to repository`() {
        val arrangement = createSampleArrangement(id = 7L, slug = "seo-bouquet")
        `when`(floralArrangementRepo.findBySeoName("seo-bouquet")).thenReturn(arrangement)

        val result = adapter.findBySeoName("seo-bouquet")

        assertNotNull(result)
        assertEquals("seo-bouquet", result?.seoName)
        verify(floralArrangementRepo).findBySeoName("seo-bouquet")
    }

    @Test
    fun `findWithCategoriesByIds should delegate to repository`() {
        val arrangements = listOf(createSampleArrangement(id = 1L), createSampleArrangement(id = 2L))
        `when`(floralArrangementRepo.findWithCategoriesByIds(listOf(1L, 2L))).thenReturn(arrangements)

        val result = adapter.findWithCategoriesByIds(listOf(1L, 2L))

        assertEquals(2, result.size)
        verify(floralArrangementRepo).findWithCategoriesByIds(listOf(1L, 2L))
    }

    @Test
    fun `findPrimaryImagesByArrangementIds should delegate to repository`() {
        val galleryItem = ProductGallery(
            id = 10L,
            publicId = "img-primary",
            originalUrl = "https://orig.jpg",
            thumbnailUrl = "https://thumb.jpg",
            mediumUrl = "https://med.jpg",
            altText = "Alt",
            isPrimary = true,
            position = 0
        )
        `when`(floralArrangementRepo.findPrimaryImagesByArrangementIds(listOf(1L, 2L))).thenReturn(listOf(galleryItem))

        val result = adapter.findPrimaryImagesByArrangementIds(listOf(1L, 2L))

        assertEquals(1, result.size)
        assertEquals("img-primary", result.first().publicId)
        verify(floralArrangementRepo).findPrimaryImagesByArrangementIds(listOf(1L, 2L))
    }

    @Test
    fun `findWithTagsByIds should delegate to repository`() {
        val arrangements = listOf(createSampleArrangement(id = 3L))
        `when`(floralArrangementRepo.findWithTagsByIds(listOf(3L))).thenReturn(arrangements)

        val result = adapter.findWithTagsByIds(listOf(3L))

        assertEquals(1, result.size)
        verify(floralArrangementRepo).findWithTagsByIds(listOf(3L))
    }
}

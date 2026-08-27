package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.api

import com.floristeriaakasia.backend.feature.floralArrangment.application.AddImageToFloralArrangementUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementCommand
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.application.GetFloralArrangementsUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.*
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import com.floristeriaakasia.backend.global.exeption.FloralArrangementSlugNotFoundException
import com.floristeriaakasia.backend.util.ApiResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class FloralArrangementControllerTest {

    @Mock
    private lateinit var createFloralArrangementUseCase: CreateFloralArrangementUseCase

    @Mock
    private lateinit var addImageToFloralArrangementUseCase: AddImageToFloralArrangementUseCase

    @Mock
    private lateinit var getFloralArrangementsUseCase: GetFloralArrangementsUseCase

    private lateinit var controller: FloralArrangementController

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObject(): T {
        any<T>()
        return null as T
    }

    @BeforeEach
    fun setUp() {
        controller = FloralArrangementController(
            createFloralArrangementUseCase = createFloralArrangementUseCase,
            addImageToFloralArrangementUseCase = addImageToFloralArrangementUseCase,
            getFloralArrangementsUseCase = getFloralArrangementsUseCase
        )
    }

    private fun createSampleDetailDto(
        id: Long = 1L,
        name: String = "Rosas Rojas",
        slug: String = "rosas-rojas",
        seoName: String = "rosas-rojas-seo"
    ): FloralArrangementDetailDto {
        return FloralArrangementDetailDto(
            id = id,
            name = name,
            slug = slug,
            seoName = seoName,
            price = PriceSummaryDto(
                amount = BigDecimal("150000.00"),
                discountAmount = BigDecimal("120000.00"),
                currency = "COP",
                hasDiscount = true,
                discountPercent = 20
            ),
            isAvailable = true,
            seasonal = false,
            featured = true,
            views = 42,
            description = DescriptionDto(
                shortDescription = "Hermoso ramo de rosas",
                description = "Hermoso ramo de rosas rojas frescas para cualquier ocasión especial."
            ),
            gallery = listOf(
                ImageDto(
                    publicId = "img_1",
                    originalUrl = "https://example.com/original.jpg",
                    thumbnailUrl = "https://example.com/thumb.jpg",
                    mediumUrl = "https://example.com/medium.jpg",
                    altText = "Rosas Rojas",
                    isPrimary = true,
                    position = 0
                )
            ),
            flowers = listOf(
                FlowerDto(id = 1L, name = "Rosa", meaning = "Amor")
            ),
            categories = listOf(
                CategorySummaryDto(id = 1L, name = "Flores", slug = "flores", path = "/1/")
            ),
            tags = listOf(
                TagSummaryDto(id = 1L, text = "Romance", route = "romance")
            ),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleSummaryDto(
        id: Long = 1L,
        name: String = "Rosas Rojas",
        slug: String = "rosas-rojas",
        seoName: String = "rosas-rojas-seo"
    ): FloralArrangementSummaryDto {
        return FloralArrangementSummaryDto(
            id = id,
            name = name,
            slug = slug,
            seoName = seoName,
            price = PriceSummaryDto(
                amount = BigDecimal("150000.00"),
                discountAmount = BigDecimal("120000.00"),
                currency = "COP",
                hasDiscount = true,
                discountPercent = 20
            ),
            isAvailable = true,
            seasonal = false,
            featured = true,
            primaryImage = ImageDto(
                publicId = "img_1",
                originalUrl = "https://example.com/original.jpg",
                thumbnailUrl = "https://example.com/thumb.jpg",
                mediumUrl = "https://example.com/medium.jpg",
                altText = "Rosas Rojas",
                isPrimary = true,
                position = 0
            ),
            categories = listOf(
                CategorySummaryDto(id = 1L, name = "Flores", slug = "flores", path = "/1/")
            ),
            tags = listOf(
                TagSummaryDto(id = 1L, text = "Romance", route = "romance")
            )
        )
    }

    @Nested
    @DisplayName("getArrangements Endpoint Tests")
    inner class GetArrangementsTests {

        @Test
        fun `should return arrangements with default pagination and sorting parameters`() {
            val summary = createSampleSummaryDto()
            val expectedPage = PageImpl(listOf(summary), PageRequest.of(0, 12, Sort.by(Sort.Direction.DESC, "views")), 1)

            var capturedQuery: FloralArrangementQuery? = null
            var capturedPageable: Pageable? = null
            `when`(getFloralArrangementsUseCase.execute(anyObject(), anyObject())).thenAnswer { invocation ->
                capturedQuery = invocation.getArgument(0) as FloralArrangementQuery
                capturedPageable = invocation.getArgument(1) as Pageable
                expectedPage
            }

            val response = controller.getArrangements(
                page = 0,
                size = 12,
                sortBy = "views",
                sortDir = "desc"
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertEquals(1, response.body?.totalElements)
            assertEquals(summary, response.body?.content?.get(0))

            assertNotNull(capturedQuery)
            assertNull(capturedQuery?.categoryId)
            assertNull(capturedQuery?.tagId)
            assertNull(capturedQuery?.featured)
            assertNull(capturedQuery?.seasonal)
            assertNull(capturedQuery?.isAvailable)
            assertNull(capturedQuery?.minPrice)
            assertNull(capturedQuery?.maxPrice)

            assertNotNull(capturedPageable)
            assertEquals(0, capturedPageable?.pageNumber)
            assertEquals(12, capturedPageable?.pageSize)
            assertEquals(Sort.by(Sort.Direction.DESC, "views"), capturedPageable?.sort)
        }

        @Test
        fun `should pass custom filter parameters and ascending sort correctly`() {
            val summary = createSampleSummaryDto()
            val expectedPage = PageImpl(listOf(summary), PageRequest.of(1, 20, Sort.by(Sort.Direction.ASC, "price")), 1)

            var capturedQuery: FloralArrangementQuery? = null
            var capturedPageable: Pageable? = null
            `when`(getFloralArrangementsUseCase.execute(anyObject(), anyObject())).thenAnswer { invocation ->
                capturedQuery = invocation.getArgument(0) as FloralArrangementQuery
                capturedPageable = invocation.getArgument(1) as Pageable
                expectedPage
            }

            val minPrice = BigDecimal("50000.00")
            val maxPrice = BigDecimal("200000.00")

            val response = controller.getArrangements(
                page = 1,
                size = 20,
                sortBy = "price",
                sortDir = "asc",
                categoryId = 10L,
                tagId = 5L,
                featured = true,
                seasonal = false,
                available = true,
                minPrice = minPrice,
                maxPrice = maxPrice
            )

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(expectedPage, response.body)

            assertNotNull(capturedQuery)
            assertEquals(10L, capturedQuery?.categoryId)
            assertEquals(5L, capturedQuery?.tagId)
            assertEquals(true, capturedQuery?.featured)
            assertEquals(false, capturedQuery?.seasonal)
            assertEquals(true, capturedQuery?.isAvailable)
            assertEquals(minPrice, capturedQuery?.minPrice)
            assertEquals(maxPrice, capturedQuery?.maxPrice)

            assertNotNull(capturedPageable)
            assertEquals(1, capturedPageable?.pageNumber)
            assertEquals(20, capturedPageable?.pageSize)
            assertEquals(Sort.by(Sort.Direction.ASC, "price"), capturedPageable?.sort)
        }

        @Test
        fun `should coerce invalid page, size, and unknown sortBy fields`() {
            val expectedPage = PageImpl(emptyList<FloralArrangementSummaryDto>())

            var capturedPageable: Pageable? = null
            `when`(getFloralArrangementsUseCase.execute(anyObject(), anyObject())).thenAnswer { invocation ->
                capturedPageable = invocation.getArgument(1) as Pageable
                expectedPage
            }

            // page < 0 -> coerced to 0, size > 50 -> coerced to 50, unknown sortBy -> fallback to "views"
            controller.getArrangements(
                page = -5,
                size = 100,
                sortBy = "invalidSortField",
                sortDir = "DESC"
            )

            assertNotNull(capturedPageable)
            assertEquals(0, capturedPageable?.pageNumber)
            assertEquals(50, capturedPageable?.pageSize)
            assertEquals(Sort.by(Sort.Direction.DESC, "views"), capturedPageable?.sort)
        }

        @Test
        fun `should coerce size below 1 to 1 and accept other allowed sort fields`() {
            val expectedPage = PageImpl(emptyList<FloralArrangementSummaryDto>())

            var capturedPageable: Pageable? = null
            `when`(getFloralArrangementsUseCase.execute(anyObject(), anyObject())).thenAnswer { invocation ->
                capturedPageable = invocation.getArgument(1) as Pageable
                expectedPage
            }

            // size < 1 -> coerced to 1, sortBy = "createdAt"
            controller.getArrangements(
                page = 0,
                size = 0,
                sortBy = "createdAt",
                sortDir = "asc"
            )

            assertNotNull(capturedPageable)
            assertEquals(0, capturedPageable?.pageNumber)
            assertEquals(1, capturedPageable?.pageSize)
            assertEquals(Sort.by(Sort.Direction.ASC, "createdAt"), capturedPageable?.sort)
        }
    }

    @Nested
    @DisplayName("getArrangementById Endpoint Tests")
    inner class GetArrangementByIdTests {

        @Test
        fun `should return 200 OK with arrangement details when found`() {
            val detailDto = createSampleDetailDto(id = 1L)
            `when`(getFloralArrangementsUseCase.executeById(1L)).thenReturn(detailDto)

            val response = controller.getArrangementById(1L)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Success)
            val body = response.body as ApiResponse.Success<FloralArrangementDetailDto>
            assertEquals(detailDto, body.data)
            verify(getFloralArrangementsUseCase).executeById(1L)
        }

        @Test
        fun `should return 404 NOT_FOUND when FloralArrangementSlugNotFoundException is thrown`() {
            `when`(getFloralArrangementsUseCase.executeById(999L))
                .thenThrow(FloralArrangementSlugNotFoundException("999"))

            val response = controller.getArrangementById(999L)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Error)
            val body = response.body as ApiResponse.Error
            assertEquals("Floral arrangement with slug 999 not found.", body.message)
            verify(getFloralArrangementsUseCase).executeById(999L)
        }
    }

    @Nested
    @DisplayName("getArrangementBySeoName Endpoint Tests")
    inner class GetArrangementBySeoNameTests {

        @Test
        fun `should return 200 OK with arrangement details when found by seoName`() {
            val detailDto = createSampleDetailDto(seoName = "rosas-rojas-seo")
            `when`(getFloralArrangementsUseCase.executeBySeoName("rosas-rojas-seo")).thenReturn(detailDto)

            val response = controller.getArrangementBySeoName("rosas-rojas-seo")

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Success)
            val body = response.body as ApiResponse.Success<FloralArrangementDetailDto>
            assertEquals(detailDto, body.data)
            verify(getFloralArrangementsUseCase).executeBySeoName("rosas-rojas-seo")
        }

        @Test
        fun `should return 404 NOT_FOUND when exception is thrown by executeBySeoName`() {
            `when`(getFloralArrangementsUseCase.executeBySeoName("non-existent-seo"))
                .thenThrow(RuntimeException("Arrangement not found for SEO name"))

            val response = controller.getArrangementBySeoName("non-existent-seo")

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Error)
            val body = response.body as ApiResponse.Error
            assertEquals("Arrangement not found for SEO name", body.message)
            verify(getFloralArrangementsUseCase).executeBySeoName("non-existent-seo")
        }
    }

    @Nested
    @DisplayName("getArrangementBySlug Endpoint Tests")
    inner class GetArrangementBySlugTests {

        @Test
        fun `should return 200 OK with arrangement details when found by slug`() {
            val detailDto = createSampleDetailDto(slug = "rosas-rojas")
            `when`(getFloralArrangementsUseCase.executeBySlug("rosas-rojas")).thenReturn(detailDto)

            val response = controller.getArrangementBySlug("rosas-rojas")

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Success)
            val body = response.body as ApiResponse.Success<FloralArrangementDetailDto>
            assertEquals(detailDto, body.data)
            verify(getFloralArrangementsUseCase).executeBySlug("rosas-rojas")
        }

        @Test
        fun `should return 404 NOT_FOUND when FloralArrangementNotFoundException is thrown`() {
            `when`(getFloralArrangementsUseCase.executeBySlug("non-existent-slug"))
                .thenThrow(FloralArrangementNotFoundException(123L))

            val response = controller.getArrangementBySlug("non-existent-slug")

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertNotNull(response.body)
            assertTrue(response.body is ApiResponse.Error)
            val body = response.body as ApiResponse.Error
            assertEquals("Floral arrangement with ID 123 not found.", body.message)
            verify(getFloralArrangementsUseCase).executeBySlug("non-existent-slug")
        }
    }

    @Nested
    @DisplayName("createArrangement Endpoint Tests")
    inner class CreateArrangementTests {

        @Test
        fun `should create arrangement and return 201 CREATED`() {
            val request = CreateFloralArrangementRequest(
                name = "Orquídeas Blancas",
                seoName = "orquideas-blancas",
                categoryIds = setOf(1L, 2L),
                price = BigDecimal("250000.00"),
                discountPrice = BigDecimal("200000.00"),
                currency = "COP",
                isAvailable = true,
                seasonal = false,
                featured = true,
                shortDescription = "Hermosas orquídeas blancas",
                description = "Hermosas orquídeas blancas ideales para regalos especiales",
                flowers = mutableSetOf(
                    CreateFloralArrangementRequest.FlowerRequest(name = "Orquídea", meaning = "Pureza")
                ),
                tagIds = setOf(3L, 4L)
            )

            var capturedCommand: CreateFloralArrangementCommand? = null
            `when`(createFloralArrangementUseCase.execute(anyObject())).thenAnswer { invocation ->
                capturedCommand = invocation.getArgument(0) as CreateFloralArrangementCommand
                1L
            }

            val response = controller.createArrangement(request)

            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertNull(response.body)

            assertNotNull(capturedCommand)
            assertEquals(request.name, capturedCommand?.name)
            assertEquals(request.seoName, capturedCommand?.seoName)
            assertEquals(request.categoryIds, capturedCommand?.categoryIds)
            assertEquals(request.price, capturedCommand?.priceAmount)
            assertEquals(request.discountPrice, capturedCommand?.discountPriceAmount)
            assertEquals(request.currency, capturedCommand?.currency)
            assertEquals(request.isAvailable, capturedCommand?.isAvailable)
            assertEquals(request.seasonal, capturedCommand?.seasonal)
            assertEquals(request.featured, capturedCommand?.featured)
            assertEquals(request.shortDescription, capturedCommand?.shortDescription)
            assertEquals(request.description, capturedCommand?.description)
            assertEquals(1, capturedCommand?.flowers?.size)
            assertEquals("Orquídea", capturedCommand?.flowers?.get(0)?.name)
            assertEquals("Pureza", capturedCommand?.flowers?.get(0)?.meaning)
            assertEquals(request.tagIds, capturedCommand?.tagIds)
        }

        @Test
        fun `should return 400 BAD_REQUEST when use case throws exception`() {
            val request = CreateFloralArrangementRequest(
                name = "Orquídeas Blancas",
                seoName = "orquideas-blancas",
                categoryIds = setOf(1L),
                price = BigDecimal("250000.00"),
                currency = "COP",
                isAvailable = true,
                seasonal = false,
                featured = false,
                shortDescription = "Short desc",
                description = "Long desc"
            )

            `when`(createFloralArrangementUseCase.execute(anyObject()))
                .thenThrow(IllegalArgumentException("Invalid arrangement data"))

            val response = controller.createArrangement(request)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals("Invalid arrangement data", response.body)
        }
    }

    @Nested
    @DisplayName("uploadFloralArrangementImage Endpoint Tests")
    inner class UploadFloralArrangementImageTests {

        @Test
        fun `should upload image and return 201 CREATED`() {
            val file = MockMultipartFile(
                "file",
                "roses.jpg",
                "image/jpeg",
                "test image content".toByteArray()
            )
            val arrangementId = 10L
            val altText = "Foto de rosas rojas"

            val response = controller.uploadFloralArrangementImage(
                floralArrangementId = arrangementId,
                file = file,
                altText = altText
            )

            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertNull(response.body)
            verify(addImageToFloralArrangementUseCase).execute(arrangementId, file, altText)
        }

        @Test
        fun `should return 400 BAD_REQUEST when upload fails`() {
            val file = MockMultipartFile(
                "file",
                "roses.jpg",
                "image/jpeg",
                "test image content".toByteArray()
            )
            val arrangementId = 10L
            val altText = "Foto de rosas rojas"

            `when`(addImageToFloralArrangementUseCase.execute(arrangementId, file, altText))
                .thenThrow(RuntimeException("Cloudinary upload failed"))

            val response = controller.uploadFloralArrangementImage(
                floralArrangementId = arrangementId,
                file = file,
                altText = altText
            )

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals("Cloudinary upload failed", response.body)
            verify(addImageToFloralArrangementUseCase).execute(arrangementId, file, altText)
        }
    }
}

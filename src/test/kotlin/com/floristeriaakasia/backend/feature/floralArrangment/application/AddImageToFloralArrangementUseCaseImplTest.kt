package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

class AddImageToFloralArrangementUseCaseImplTest {

    private lateinit var imageStorage: ImageStoragePort
    private lateinit var saveFloralArrangementPort: SaveFloralArrangementPort
    private lateinit var useCase: AddImageToFloralArrangementUseCaseImpl

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObject(): T {
        any<T>()
        return null as T
    }

    @BeforeEach
    fun setUp() {
        imageStorage = mock(ImageStoragePort::class.java)
        saveFloralArrangementPort = mock(SaveFloralArrangementPort::class.java)
        useCase = AddImageToFloralArrangementUseCaseImpl(imageStorage, saveFloralArrangementPort)
    }

    private fun createFloral(id: Long = 1L, slug: String = "test-arrangement") = FloralArrangement(
        id = id,
        name = "Test Arrangement",
        seoName = slug,
        slug = slug,
        price = Price(price = BigDecimal("50000.00"), currency = "COP"),
        isAvailable = true,
        seasonal = false,
        featured = false
    )

    @Test
    fun `should upload image and add as primary when gallery is empty`() {
        val arrangementId = 1L
        val file = mock(MultipartFile::class.java)
        val altText = "Floral Arrangement Image"
        val slug = "test-arrangement"
        val floral = createFloral(id = arrangementId, slug = slug)

        val uploadedImage = UploadedImageDto(
            publicId = "public-id-1",
            original = "original-url-1",
            thumbnail = "thumbnail-url-1",
            medium = "medium-url-1"
        )

        `when`(saveFloralArrangementPort.findById(arrangementId)).thenReturn(floral)
        `when`(imageStorage.uploadImage(file, "floralArrangements/$slug")).thenReturn(uploadedImage)

        useCase.execute(arrangementId, file, altText)

        assertEquals(1, floral.gallery.size)
        val galleryItem = floral.gallery.first()
        assertEquals("public-id-1", galleryItem.publicId)
        assertEquals("original-url-1", galleryItem.originalUrl)
        assertEquals("thumbnail-url-1", galleryItem.thumbnailUrl)
        assertEquals("medium-url-1", galleryItem.mediumUrl)
        assertEquals(altText, galleryItem.altText)
        assertTrue(galleryItem.isPrimary)
        assertEquals(0, galleryItem.position)
        assertEquals(floral, galleryItem.floralArrangement)

        verify(saveFloralArrangementPort).save(floral)
    }

    @Test
    fun `should add subsequent image with correct position and isPrimary false`() {
        val arrangementId = 2L
        val file = mock(MultipartFile::class.java)
        val altText = "Second Angle"
        val slug = "bouquet-primavera"
        val floral = createFloral(id = arrangementId, slug = slug)

        val existingImage = ProductGallery(
            id = 10L,
            publicId = "img-existing",
            originalUrl = "https://orig.jpg",
            thumbnailUrl = "https://thumb.jpg",
            mediumUrl = "https://med.jpg",
            altText = "First Image",
            isPrimary = true,
            position = 0,
            floralArrangement = floral
        )
        floral.gallery.add(existingImage)

        val uploadedImage = UploadedImageDto(
            publicId = "img-second",
            original = "https://orig2.jpg",
            thumbnail = "https://thumb2.jpg",
            medium = "https://med2.jpg"
        )

        `when`(saveFloralArrangementPort.findById(arrangementId)).thenReturn(floral)
        `when`(imageStorage.uploadImage(file, "floralArrangements/$slug")).thenReturn(uploadedImage)

        useCase.execute(arrangementId, file, altText)

        assertEquals(2, floral.gallery.size)
        val secondItem = floral.gallery.first { it.publicId == "img-second" }
        assertEquals(1, secondItem.position)
        assertFalse(secondItem.isPrimary)
        assertEquals(altText, secondItem.altText)
        assertEquals(floral, secondItem.floralArrangement)

        verify(saveFloralArrangementPort).save(floral)
    }

    @Test
    fun `should throw FloralArrangementNotFoundException when arrangement does not exist`() {
        val nonExistentId = 999L
        val file = mock(MultipartFile::class.java)

        `when`(saveFloralArrangementPort.findById(nonExistentId)).thenReturn(null)

        val ex = assertThrows<FloralArrangementNotFoundException> {
            useCase.execute(nonExistentId, file, "Some alt text")
        }

        assertTrue(ex.message!!.contains("999"))
        verifyNoInteractions(imageStorage)
        verify(saveFloralArrangementPort, never()).save(anyObject())
    }

    @Test
    fun `should propagate exception and not save when image upload fails`() {
        val arrangementId = 1L
        val file = mock(MultipartFile::class.java)
        val floral = createFloral(id = arrangementId, slug = "test-slug")

        `when`(saveFloralArrangementPort.findById(arrangementId)).thenReturn(floral)
        `when`(imageStorage.uploadImage(file, "floralArrangements/test-slug"))
            .thenThrow(IllegalStateException("Cloudinary storage unavailable"))

        val ex = assertThrows<IllegalStateException> {
            useCase.execute(arrangementId, file, "Alt")
        }

        assertEquals("Cloudinary storage unavailable", ex.message)
        assertEquals(0, floral.gallery.size)
        verify(saveFloralArrangementPort, never()).save(anyObject())
    }
}

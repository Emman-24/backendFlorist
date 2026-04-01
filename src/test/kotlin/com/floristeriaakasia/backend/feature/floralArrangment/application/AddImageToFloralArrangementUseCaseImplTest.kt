package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

class AddImageToFloralArrangementUseCaseImplTest {

    private lateinit var imageStorage: ImageStoragePort
    private lateinit var saveFloralArrangementPort: SaveFloralArrangementPort
    private lateinit var useCase: AddImageToFloralArrangementUseCaseImpl

    @BeforeEach
    fun setUp() {
        imageStorage = mock(ImageStoragePort::class.java)
        saveFloralArrangementPort = mock(SaveFloralArrangementPort::class.java)
        useCase = AddImageToFloralArrangementUseCaseImpl(imageStorage, saveFloralArrangementPort)
    }

    @Test
    fun `should upload image and add to floral arrangement gallery`() {
        val arrangementId = 1L
        val file = mock(MultipartFile::class.java)
        val altText = "Floral Arrangement Image"
        val slug = "test-arrangement"
        
        val floral = FloralArrangement(
            id = arrangementId,
            name = "Test Arrangement",
            seoName = "test-arrangement",
            slug = slug,
            price = Price(price = BigDecimal.TEN, currency = "USD"),
            isAvailable = true,
            seasonal = false,
            featured = false
        )

        val uploadedImage = UploadedImageDto(
            publicId = "public-id",
            original = "original-url",
            thumbnail = "thumbnail-url",
            medium = "medium-url"
        )

        `when`(saveFloralArrangementPort.findById(arrangementId)).thenReturn(floral)
        `when`(imageStorage.uploadImage(file, "floralArrangements/$slug")).thenReturn(uploadedImage)

        useCase.execute(arrangementId, file, altText)

        assertEquals(1, floral.gallery.size)
        val galleryItem = floral.gallery.first()
        assertEquals("public-id", galleryItem.publicId)
        assertEquals("original-url", galleryItem.originalUrl)
        assertEquals(altText, galleryItem.altText)
        assertEquals(floral, galleryItem.floralArrangement)

        verify(saveFloralArrangementPort).save(floral)
    }
}

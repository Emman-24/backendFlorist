package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.cloudinary.Uploader
import com.cloudinary.Url
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.*
import org.springframework.web.multipart.MultipartFile

class CloudinaryImageAdapterTest {

    private lateinit var cloudinary: Cloudinary
    private lateinit var uploader: Uploader
    private lateinit var url: Url
    private lateinit var adapter: CloudinaryImageAdapter

    @BeforeEach
    fun setUp() {
        cloudinary = mock(Cloudinary::class.java)
        uploader = mock(Uploader::class.java)
        url = mock(Url::class.java)

        `when`(cloudinary.uploader()).thenReturn(uploader)
        `when`(cloudinary.url()).thenReturn(url)
        `when`(url.transformation(any(Transformation::class.java))).thenReturn(url)
        `when`(url.version(anyInt())).thenReturn(url)
        `when`(url.secure(true)).thenReturn(url)

        adapter = CloudinaryImageAdapter(cloudinary)
    }

    @Test
    fun `uploadImage should upload file bytes and return UploadedImageDto with generated URLs`() {
        val file = mock(MultipartFile::class.java)
        val fileBytes = "image-bytes".toByteArray()
        `when`(file.bytes).thenReturn(fileBytes)

        val uploadResponse: Map<String, Any> = mapOf(
            "public_id" to "floristeria-akasia/floralArrangements/test-slug/img_123",
            "version" to 123456789
        )
        `when`(uploader.upload(eq(fileBytes), anyMap<String, Any>())).thenReturn(uploadResponse)
        `when`(url.generate("floristeria-akasia/floralArrangements/test-slug/img_123"))
            .thenReturn("https://res.cloudinary.com/demo/image/upload/v123456789/floristeria-akasia/floralArrangements/test-slug/img_123.jpg")

        val result = adapter.uploadImage(file, "floralArrangements/test-slug")

        assertNotNull(result)
        assertEquals("floristeria-akasia/floralArrangements/test-slug/img_123", result.publicId)
        assertEquals("https://res.cloudinary.com/demo/image/upload/v123456789/floristeria-akasia/floralArrangements/test-slug/img_123.jpg", result.original)
        assertNotNull(result.thumbnail)
        assertNotNull(result.medium)

        verify(uploader).upload(eq(fileBytes), anyMap<String, Any>())
    }

    @Test
    fun `uploadImage should wrap any exception into IllegalStateException`() {
        val file = mock(MultipartFile::class.java)
        `when`(file.bytes).thenThrow(RuntimeException("I/O error reading file"))

        val ex = assertThrows<IllegalStateException> {
            adapter.uploadImage(file, "folder")
        }

        assertTrue(ex.message!!.contains("Cloudinary upload failed: I/O error reading file"))
        assertTrue(ex.cause is RuntimeException)
    }

    @Test
    fun `deleteImage should call destroy on Cloudinary uploader`() {
        `when`(uploader.destroy(eq("pub-id-123"), anyMap<String, Any>())).thenReturn(mapOf("result" to "ok"))

        adapter.deleteImage("pub-id-123")

        verify(uploader).destroy("pub-id-123", emptyMap<String, Any>())
    }

    @Test
    fun `deleteImage should wrap any exception into IllegalStateException`() {
        `when`(uploader.destroy(eq("pub-id-456"), anyMap<String, Any>()))
            .thenThrow(RuntimeException("Cloudinary API unreachable"))

        val ex = assertThrows<IllegalStateException> {
            adapter.deleteImage("pub-id-456")
        }

        assertTrue(ex.message!!.contains("Cloudinary deletion failed: Cloudinary API unreachable"))
        assertTrue(ex.cause is RuntimeException)
    }
}

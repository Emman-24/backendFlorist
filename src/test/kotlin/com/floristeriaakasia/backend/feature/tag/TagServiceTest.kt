package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TagServiceTest {

    @Mock
    private lateinit var floralArrangementRepository: FloralArrangementRepository

    @Mock
    private lateinit var tagRepository: TagRepository

    @InjectMocks
    private lateinit var tagService: TagService


    @Test
    fun `should create tag when route does not already exist`() {
        // Arrange
        val request = CreateTagRequest(text = "Rosas", route = "rosas", status = true)
        Mockito.`when`(tagRepository.findByRoute("rosas")).thenReturn(null)
        Mockito.`when`(tagRepository.save(Mockito.any())).thenAnswer {
            (it.getArgument(0) as Tag).apply { id = 1L }
        }

        // Act
        val result = tagService.createTag(request)

        // Assert
        assertEquals(1L, result.id)
        assertEquals("Rosas", result.text)
        assertEquals("rosas", result.route)
        assertEquals("", result.description)
        assertTrue(result.status)
    }

    @Test
    fun `should trim text and normalize route before checking duplicates and saving`() {
        // Arrange
        val request = CreateTagRequest(text = "  Rosas  ", route = "  ROSAS  ", status = true)
        Mockito.`when`(tagRepository.findByRoute("rosas")).thenReturn(null)
        Mockito.`when`(tagRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = tagService.createTag(request)

        // Assert
        assertEquals("Rosas", result.text)
        assertEquals("rosas", result.route)
        // the duplicate check must use the same normalized value that gets persisted
        Mockito.verify(tagRepository).findByRoute("rosas")
    }

    @Test
    fun `should throw IllegalArgumentException when a tag with the same route already exists`() {
        // Arrange
        val request = CreateTagRequest(text = "Rosas", route = "rosas", status = true)
        val existing = buildTag(id = 5L, text = "Rosas", route = "rosas")
        Mockito.`when`(tagRepository.findByRoute("rosas")).thenReturn(existing)

        // Act
        val exception = assertFailsWith<IllegalArgumentException> {
            tagService.createTag(request)
        }

        // Assert
        assertTrue(exception.message!!.contains("rosas"))
        Mockito.verify(tagRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `should detect a duplicate route even when the request casing differs`() {
        // Arrange — an existing tag stored as "rosas", request comes in uppercase
        val request = CreateTagRequest(text = "Rosas", route = "ROSAS", status = true)
        val existing = buildTag(id = 5L, text = "Rosas", route = "rosas")
        Mockito.`when`(tagRepository.findByRoute("rosas")).thenReturn(existing)

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            tagService.createTag(request)
        }
        Mockito.verify(tagRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `should default description to blank and respect the requested status`() {
        // Arrange
        val request = CreateTagRequest(text = "Tulipanes", route = "tulipanes", status = false)
        Mockito.`when`(tagRepository.findByRoute("tulipanes")).thenReturn(null)
        Mockito.`when`(tagRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = tagService.createTag(request)

        // Assert
        assertEquals("", result.description)
        assertFalse(result.status)
    }


    @Test
    fun `should return all tags from the repository`() {
        // Arrange
        val tags = listOf(
            buildTag(id = 1L, text = "Rosas", route = "rosas"),
            buildTag(id = 2L, text = "Tulipanes", route = "tulipanes")
        )
        Mockito.`when`(tagRepository.findAll()).thenReturn(tags)

        // Act
        val result = tagService.getAllTags()

        // Assert
        assertEquals(tags, result)
    }

    @Test
    fun `should return an empty list when no tags exist`() {
        // Arrange
        Mockito.`when`(tagRepository.findAll()).thenReturn(emptyList())

        // Act
        val result = tagService.getAllTags()

        // Assert
        assertTrue(result.isEmpty())
    }


    @Test
    fun `should replace existing tags with the provided tag ids`() {
        // Arrange
        val productId = 1L
        val oldTag = buildTag(id = 10L, text = "Old", route = "old")
        val newTag1 = buildTag(id = 1L, text = "Rosas", route = "rosas")
        val newTag2 = buildTag(id = 2L, text = "Tulipanes", route = "tulipanes")
        val floral = buildFloralArrangement(id = productId).apply { tags.add(oldTag) }

        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))
        Mockito.`when`(tagRepository.findAllById(listOf(1L, 2L))).thenReturn(listOf(newTag1, newTag2))
        Mockito.`when`(floralArrangementRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = tagService.assignTags(productId, listOf(1L, 2L))

        // Assert
        assertTrue(result)
        assertEquals(setOf(newTag1, newTag2), floral.tags)
        assertFalse(floral.tags.contains(oldTag))
        Mockito.verify(floralArrangementRepository).save(floral)
    }

    @Test
    fun `should clear all tags when an empty tag id list is provided`() {
        // Arrange
        val productId = 1L
        val oldTag = buildTag(id = 10L, text = "Old", route = "old")
        val floral = buildFloralArrangement(id = productId).apply { tags.add(oldTag) }

        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))
        Mockito.`when`(tagRepository.findAllById(emptyList())).thenReturn(emptyList())
        Mockito.`when`(floralArrangementRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = tagService.assignTags(productId, emptyList())

        // Assert
        assertTrue(result)
        assertTrue(floral.tags.isEmpty())
    }

    @Test
    fun `should deduplicate tags when the tag repository returns the same tag more than once`() {
        // Arrange
        val productId = 1L
        val tag = buildTag(id = 1L, text = "Rosas", route = "rosas")
        val floral = buildFloralArrangement(id = productId)

        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))
        Mockito.`when`(tagRepository.findAllById(listOf(1L, 1L))).thenReturn(listOf(tag, tag))
        Mockito.`when`(floralArrangementRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        tagService.assignTags(productId, listOf(1L, 1L))

        // Assert — Set semantics on FloralArrangement.tags prevent duplicates
        assertEquals(1, floral.tags.size)
    }

    @Test
    fun `should silently skip tag ids that do not exist in the tag repository`() {
        // Arrange
        val productId = 1L
        val existingTag = buildTag(id = 1L, text = "Rosas", route = "rosas")
        val floral = buildFloralArrangement(id = productId)

        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))
        Mockito.`when`(tagRepository.findAllById(listOf(1L, 999L))).thenReturn(listOf(existingTag))
        Mockito.`when`(floralArrangementRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) }

        // Act
        val result = tagService.assignTags(productId, listOf(1L, 999L))

        // Assert
        assertTrue(result)
        assertEquals(setOf(existingTag), floral.tags)
    }

    @Test
    fun `should throw FloralArrangementNotFoundException when the floral arrangement does not exist`() {
        // Arrange
        val productId = 999L
        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.empty())

        // Act
        val exception = assertFailsWith<FloralArrangementNotFoundException> {
            tagService.assignTags(productId, listOf(1L, 2L))
        }

        // Assert — fails fast, never touches tags or persists anything
        assertTrue(exception.message!!.contains("999"))
        Mockito.verify(tagRepository, Mockito.never()).findAllById(Mockito.any<List<Long>>())
        Mockito.verify(floralArrangementRepository, Mockito.never()).save(Mockito.any())
    }


    @Test
    fun `should return the tags belonging to an existing floral arrangement`() {
        // Arrange
        val productId = 1L
        val tag1 = buildTag(id = 1L, text = "Rosas", route = "rosas")
        val tag2 = buildTag(id = 2L, text = "Tulipanes", route = "tulipanes")
        val floral = buildFloralArrangement(id = productId).apply {
            tags.add(tag1)
            tags.add(tag2)
        }
        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))

        // Act
        val result = tagService.getProductTags(productId)

        // Assert
        assertEquals(setOf(tag1, tag2), result.toSet())
    }

    @Test
    fun `should return an empty list when the floral arrangement does not exist`() {
        // Arrange
        val productId = 999L
        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.empty())

        // Act
        val result = tagService.getProductTags(productId)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return an empty list when the floral arrangement has no tags`() {
        // Arrange
        val productId = 1L
        val floral = buildFloralArrangement(id = productId)
        Mockito.`when`(floralArrangementRepository.findById(productId)).thenReturn(Optional.of(floral))

        // Act
        val result = tagService.getProductTags(productId)

        // Assert
        assertTrue(result.isEmpty())
    }


    private fun buildTag(
        id: Long = 1L,
        text: String = "Rosas",
        route: String = "rosas",
        status: Boolean = true
    ) = Tag(
        id = id,
        text = text,
        route = route,
        description = "",
        status = status
    )

    private fun buildFloralArrangement(id: Long = 1L) = FloralArrangement(
        id = id,
        name = "Test Arrangement",
        seoName = "test-arrangement",
        slug = "test-arrangement-$id",
        price = Price(price = BigDecimal.TEN, currency = "USD"),
        isAvailable = true,
        seasonal = false,
        featured = false
    )
}
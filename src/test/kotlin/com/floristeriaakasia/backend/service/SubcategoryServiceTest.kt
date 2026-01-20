package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class SubcategoryServiceTest {

    private lateinit var subcategoryService: SubcategoryService
    private lateinit var subcategoryRepository: SubcategoryRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var seoUrlService: SeoUrlService

    @BeforeEach
    fun setUp() {
        subcategoryRepository = mock()
        categoryRepository = mock()
        seoUrlService = mock()
        subcategoryService = SubcategoryService(subcategoryRepository, categoryRepository, seoUrlService)
    }

    // Test data helpers
    private fun createCategory(
        id: Long? = 1L,
        text: String = "Test Category",
        route: String = "test-category"
    ): Category {
        return Category(text, route, "Test Description", 1, true).apply {
            this.id = id
        }
    }

    private fun createSubcategory(
        id: Long? = 1L,
        text: String = "Test Subcategory",
        route: String = "test-subcategory",
        description: String = "Test Description",
        position: Int = 1,
        status: Boolean = true,
        category: Category = createCategory()
    ): SubCategory {
        val subcategory = SubCategory(text, description, position, route, status)
        subcategory.category = category
        // Use reflection to set the id field since it's immutable
        if (id != null) {
            val idField = SubCategory::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(subcategory, id)
        }
        return subcategory
    }

    // findAll() tests
    @Test
    fun `findAll should return all subcategories sorted by position`() {
        val subcategory1 = createSubcategory(id = 1L, text = "Subcategory 1", position = 2)
        val subcategory2 = createSubcategory(id = 2L, text = "Subcategory 2", position = 1)
        val subcategory3 = createSubcategory(id = 3L, text = "Subcategory 3", position = 3)
        val subcategories = listOf(subcategory1, subcategory2, subcategory3)

        `when`(subcategoryRepository.findAll()).thenReturn(subcategories)

        val result = subcategoryService.findAll()

        assertEquals(3, result.size)
        assertEquals(subcategory2.id, result[0].id) // position 1
        assertEquals(subcategory1.id, result[1].id) // position 2
        assertEquals(subcategory3.id, result[2].id) // position 3
        verify(subcategoryRepository).findAll()
    }

    @Test
    fun `findAll should return empty list when no subcategories exist`() {
        `when`(subcategoryRepository.findAll()).thenReturn(emptyList())

        val result = subcategoryService.findAll()

        assertTrue(result.isEmpty())
        verify(subcategoryRepository).findAll()
    }

    // findByCategoryId() tests
    @Test
    fun `findByCategoryId should return active subcategories for given category ordered by position`() {
        val categoryId = 1L
        val subcategory1 = createSubcategory(id = 1L, text = "Subcategory 1", position = 1, status = true)
        val subcategory2 = createSubcategory(id = 2L, text = "Subcategory 2", position = 2, status = true)
        val subcategories = listOf(subcategory1, subcategory2)

        `when`(subcategoryRepository.findByCategoryIdAndStatusOrderByPositionAsc(categoryId, true))
            .thenReturn(subcategories)

        val result = subcategoryService.findByCategoryId(categoryId)

        assertEquals(2, result.size)
        assertTrue(result.all { it.status })
        assertEquals(subcategory1.id, result[0].id)
        assertEquals(subcategory2.id, result[1].id)
        verify(subcategoryRepository).findByCategoryIdAndStatusOrderByPositionAsc(categoryId, true)
    }

    @Test
    fun `findByCategoryId should return empty list when no active subcategories exist for category`() {
        val categoryId = 1L

        `when`(subcategoryRepository.findByCategoryIdAndStatusOrderByPositionAsc(categoryId, true))
            .thenReturn(emptyList())

        val result = subcategoryService.findByCategoryId(categoryId)

        assertTrue(result.isEmpty())
        verify(subcategoryRepository).findByCategoryIdAndStatusOrderByPositionAsc(categoryId, true)
    }

    // findById() tests
    @Test
    fun `findById should return subcategory when it exists`() {
        val subcategory = createSubcategory(id = 1L)

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory))

        val result = subcategoryService.findById(1L)

        assertNotNull(result)
        assertEquals(subcategory.id, result?.id)
        assertEquals(subcategory.text, result?.text)
        verify(subcategoryRepository).findById(1L)
    }

    @Test
    fun `findById should return null when subcategory does not exist`() {
        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.empty())

        val result = subcategoryService.findById(1L)

        assertNull(result)
        verify(subcategoryRepository).findById(1L)
    }

    // findByRoute() tests
    @Test
    fun `findByRoute should return subcategory when route exists`() {
        val subcategory = createSubcategory(route = "test-route")

        `when`(subcategoryRepository.findByRoute("test-route")).thenReturn(subcategory)

        val result = subcategoryService.findByRoute("test-route")

        assertNotNull(result)
        assertEquals(subcategory.route, result?.route)
        verify(subcategoryRepository).findByRoute("test-route")
    }

    @Test
    fun `findByRoute should return null when route does not exist`() {
        `when`(subcategoryRepository.findByRoute("non-existent-route")).thenReturn(null)

        val result = subcategoryService.findByRoute("non-existent-route")

        assertNull(result)
        verify(subcategoryRepository).findByRoute("non-existent-route")
    }

    // save() tests
    @Test
    fun `save should save subcategory and create SEO URL`() {
        val subcategory = createSubcategory(id = null)
        val savedSubcategory = createSubcategory(id = 1L)

        `when`(subcategoryRepository.save(subcategory)).thenReturn(savedSubcategory)

        val result = subcategoryService.save(subcategory)

        assertNotNull(result.id)
        assertEquals(savedSubcategory.id, result.id)
        verify(subcategoryRepository).save(subcategory)
        verify(seoUrlService).createOrUpdateSubCategoryUrl(savedSubcategory)
    }

    // update() tests
    @Test
    fun `update should update existing subcategory and update SEO URL`() {
        val existingSubcategory = createSubcategory(id = 1L, text = "Old Text", route = "old-route")
        val updatedData = createSubcategory(id = 1L, text = "New Text", route = "new-route", description = "New Description")

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.of(existingSubcategory))
        `when`(subcategoryRepository.save(existingSubcategory)).thenReturn(existingSubcategory)

        val result = subcategoryService.update(1L, updatedData)

        assertEquals("New Text", result.text)
        assertEquals("new-route", result.route)
        assertEquals("New Description", result.description)
        verify(subcategoryRepository).findById(1L)
        verify(subcategoryRepository).save(existingSubcategory)
        verify(seoUrlService).createOrUpdateSubCategoryUrl(existingSubcategory)
    }

    @Test
    fun `update should throw ResourceNotFoundException when subcategory does not exist`() {
        val updatedData = createSubcategory(id = 1L)

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            subcategoryService.update(1L, updatedData)
        }

        assertEquals("Subcategory with id 1 not found", exception.message)
        verify(subcategoryRepository).findById(1L)
        verifyNoMoreInteractions(subcategoryRepository)
        verifyNoInteractions(seoUrlService)
    }

    // deleteById() tests
    @Test
    fun `deleteById should delete subcategory when it exists`() {
        val subcategory = createSubcategory(id = 1L)

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory))

        subcategoryService.deleteById(1L)

        verify(subcategoryRepository).findById(1L)
        verify(subcategoryRepository).delete(subcategory)
    }

    @Test
    fun `deleteById should throw ResourceNotFoundException when subcategory does not exist`() {
        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            subcategoryService.deleteById(1L)
        }

        assertEquals("Subcategory with id 1 not found", exception.message)
        verify(subcategoryRepository).findById(1L)
        verifyNoMoreInteractions(subcategoryRepository)
    }

    // toggleStatus() tests
    @Test
    fun `toggleStatus should toggle status from true to false`() {
        val subcategory = createSubcategory(id = 1L, status = true)

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory))
        `when`(subcategoryRepository.save(subcategory)).thenReturn(subcategory)

        subcategoryService.toggleStatus(1L)

        assertFalse(subcategory.status)
        verify(subcategoryRepository).findById(1L)
        verify(subcategoryRepository).save(subcategory)
    }

    @Test
    fun `toggleStatus should toggle status from false to true`() {
        val subcategory = createSubcategory(id = 1L, status = false)

        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory))
        `when`(subcategoryRepository.save(subcategory)).thenReturn(subcategory)

        subcategoryService.toggleStatus(1L)

        assertTrue(subcategory.status)
        verify(subcategoryRepository).findById(1L)
        verify(subcategoryRepository).save(subcategory)
    }

    @Test
    fun `toggleStatus should throw ResourceNotFoundException when subcategory does not exist`() {
        `when`(subcategoryRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            subcategoryService.toggleStatus(1L)
        }

        assertEquals("Subcategory with id 1 not found", exception.message)
        verify(subcategoryRepository).findById(1L)
        verifyNoMoreInteractions(subcategoryRepository)
    }
}

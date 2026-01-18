package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.CategoryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    private lateinit var categoryService: CategoryService
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var seoUrlService: SeoUrlService

    @BeforeEach
    fun setUp() {
        categoryRepository = mock()
        seoUrlService = mock()
        categoryService = CategoryService(categoryRepository, seoUrlService)
    }

    // Test data helpers
    private fun createCategory(
        id: Long? = 1L,
        text: String = "Test Category",
        route: String = "test-category",
        description: String = "Test Description",
        position: Int = 1,
        status: Boolean = true
    ): Category {
        return Category(text, route, description, position, status).apply {
            this.id = id
        }
    }

    // findAll() tests
    @Test
    fun `findAll should return all categories sorted by position`() {
        val category1 = createCategory(id = 1L, text = "Category 1", position = 2)
        val category2 = createCategory(id = 2L, text = "Category 2", position = 1)
        val category3 = createCategory(id = 3L, text = "Category 3", position = 3)
        val categories = listOf(category1, category2, category3)

        `when`(categoryRepository.findAll()).thenReturn(categories)

        val result = categoryService.findAll()

        assertEquals(3, result.size)
        assertEquals(category2.id, result[0].id) // position 1
        assertEquals(category1.id, result[1].id) // position 2
        assertEquals(category3.id, result[2].id) // position 3
        verify(categoryRepository).findAll()
    }

    @Test
    fun `findAll should return empty list when no categories exist`() {
        `when`(categoryRepository.findAll()).thenReturn(emptyList())

        val result = categoryService.findAll()

        assertTrue(result.isEmpty())
        verify(categoryRepository).findAll()
    }

    // findAllActive() tests
    @Test
    fun `findAllActive should return only active categories ordered by position`() {
        val activeCategory1 = createCategory(id = 1L, text = "Active 1", position = 1, status = true)
        val activeCategory2 = createCategory(id = 2L, text = "Active 2", position = 2, status = true)
        val activeCategories = listOf(activeCategory1, activeCategory2)

        `when`(categoryRepository.findByStatusOrderByPositionAsc(true)).thenReturn(activeCategories)

        val result = categoryService.findAllActive()

        assertEquals(2, result.size)
        assertTrue(result.all { it.status })
        assertEquals(activeCategory1.id, result[0].id)
        assertEquals(activeCategory2.id, result[1].id)
        verify(categoryRepository).findByStatusOrderByPositionAsc(true)
    }

    @Test
    fun `findAllActive should return empty list when no active categories exist`() {
        `when`(categoryRepository.findByStatusOrderByPositionAsc(true)).thenReturn(emptyList())

        val result = categoryService.findAllActive()

        assertTrue(result.isEmpty())
        verify(categoryRepository).findByStatusOrderByPositionAsc(true)
    }

    // findById() tests
    @Test
    fun `findById should return category when it exists`() {
        val category = createCategory(id = 1L)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        val result = categoryService.findById(1L)

        assertNotNull(result)
        assertEquals(category.id, result?.id)
        assertEquals(category.text, result?.text)
        verify(categoryRepository).findById(1L)
    }

    @Test
    fun `findById should return null when category does not exist`() {
        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        val result = categoryService.findById(999L)

        assertNull(result)
        verify(categoryRepository).findById(999L)
    }

    // findByRoute() tests
    @Test
    fun `findByRoute should return category when route exists`() {
        val category = createCategory(route = "test-route")

        `when`(categoryRepository.findByRoute("test-route")).thenReturn(category)

        val result = categoryService.findByRoute("test-route")

        assertNotNull(result)
        assertEquals(category.route, result?.route)
        verify(categoryRepository).findByRoute("test-route")
    }

    @Test
    fun `findByRoute should return null when route does not exist`() {
        `when`(categoryRepository.findByRoute("non-existent")).thenReturn(null)

        val result = categoryService.findByRoute("non-existent")

        assertNull(result)
        verify(categoryRepository).findByRoute("non-existent")
    }

    // save() tests
    @Test
    fun `save should save category and create SEO URL`() {
        val category = createCategory(id = null)
        val savedCategory = createCategory(id = 1L)

        `when`(categoryRepository.save(category)).thenReturn(savedCategory)

        val result = categoryService.save(category)

        assertNotNull(result.id)
        assertEquals(savedCategory.id, result.id)
        verify(categoryRepository).save(category)
        verify(seoUrlService).createOrUpdateCategoryUrl(savedCategory)
    }

    // update() tests
    @Test
    fun `update should update existing category and update SEO URL`() {
        val existingCategory = createCategory(id = 1L, text = "Old Text", route = "old-route")
        val updateData = createCategory(id = null, text = "New Text", route = "new-route", description = "New Description", position = 5, status = false)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory))
        `when`(categoryRepository.save(existingCategory)).thenReturn(existingCategory)

        val result = categoryService.update(1L, updateData)

        assertEquals("New Text", result.text)
        assertEquals("new-route", result.route)
        assertEquals("New Description", result.description)
        assertEquals(5, result.position)
        assertFalse(result.status)
        verify(categoryRepository).findById(1L)
        verify(categoryRepository).save(existingCategory)
        verify(seoUrlService).createOrUpdateCategoryUrl(existingCategory)
    }

    @Test
    fun `update should throw ResourceNotFoundException when category does not exist`() {
        val updateData = createCategory(id = null, text = "New Text")

        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            categoryService.update(999L, updateData)
        }

        assertEquals("Category with id 999 not found", exception.message)
        verify(categoryRepository).findById(999L)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(seoUrlService)
    }

    // deleteById() tests
    @Test
    fun `deleteById should delete category when it has no subcategories or products`() {
        val category = createCategory(id = 1L)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        categoryService.deleteById(1L)

        verify(categoryRepository).findById(1L)
        verify(categoryRepository).delete(category)
    }

    @Test
    fun `deleteById should throw ResourceNotFoundException when category does not exist`() {
        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            categoryService.deleteById(999L)
        }

        assertEquals("Category with id 999 not found", exception.message)
        verify(categoryRepository).findById(999L)
        verifyNoMoreInteractions(categoryRepository)
    }

    @Test
    fun `deleteById should throw IllegalStateException when category has subcategories`() {
        val category = createCategory(id = 1L)
        val subCategory = SubCategory(text = "Sub", description = "desc", position = 1, route = "sub", status = true)
        subCategory.category = category
        category.subCategories.add(subCategory)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        val exception = assertThrows<IllegalStateException> {
            categoryService.deleteById(1L)
        }

        assertTrue(exception.message!!.contains("Cannot delete category with 1 subcategories"))
        verify(categoryRepository).findById(1L)
        verifyNoMoreInteractions(categoryRepository)
    }

    @Test
    fun `deleteById should throw IllegalStateException when category has products`() {
        val category = createCategory(id = 1L)
        val product = Product(title = "Product", route = "product", price = java.math.BigDecimal("100.0"), status = true)
        product.category = category
        category.products.add(product)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        val exception = assertThrows<IllegalStateException> {
            categoryService.deleteById(1L)
        }

        assertTrue(exception.message!!.contains("Cannot delete category with 0 subcategories and 1 products"))
        verify(categoryRepository).findById(1L)
        verifyNoMoreInteractions(categoryRepository)
    }

    // toggleStatus() tests
    @Test
    fun `toggleStatus should toggle status from true to false`() {
        val category = createCategory(id = 1L, status = true)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        `when`(categoryRepository.save(category)).thenReturn(category)

        val result = categoryService.toggleStatus(1L)

        assertFalse(result.status)
        verify(categoryRepository).findById(1L)
        verify(categoryRepository).save(category)
    }

    @Test
    fun `toggleStatus should toggle status from false to true`() {
        val category = createCategory(id = 1L, status = false)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        `when`(categoryRepository.save(category)).thenReturn(category)

        val result = categoryService.toggleStatus(1L)

        assertTrue(result.status)
        verify(categoryRepository).findById(1L)
        verify(categoryRepository).save(category)
    }

    @Test
    fun `toggleStatus should throw ResourceNotFoundException when category does not exist`() {
        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            categoryService.toggleStatus(999L)
        }

        assertEquals("Category with id 999 not found", exception.message)
        verify(categoryRepository).findById(999L)
        verifyNoMoreInteractions(categoryRepository)
    }

    // reorder() tests
    @Test
    fun `reorder should update positions for all provided categories`() {
        val category1 = createCategory(id = 1L, position = 1)
        val category2 = createCategory(id = 2L, position = 2)
        val category3 = createCategory(id = 3L, position = 3)
        val positions = mapOf(1L to 3, 2L to 1, 3L to 2)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category1))
        `when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category2))
        `when`(categoryRepository.findById(3L)).thenReturn(Optional.of(category3))
        `when`(categoryRepository.save(any())).thenAnswer { it.arguments[0] }

        categoryService.reorder(positions)

        assertEquals(3, category1.position)
        assertEquals(1, category2.position)
        assertEquals(2, category3.position)
        verify(categoryRepository, times(3)).findById(anyLong())
        verify(categoryRepository, times(3)).save(any())
    }

    @Test
    fun `reorder should skip non-existent categories`() {
        val category1 = createCategory(id = 1L, position = 1)
        val positions = mapOf(1L to 5, 999L to 10)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category1))
        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())
        `when`(categoryRepository.save(category1)).thenReturn(category1)

        categoryService.reorder(positions)

        assertEquals(5, category1.position)
        verify(categoryRepository).findById(1L)
        verify(categoryRepository).findById(999L)
        verify(categoryRepository, times(1)).save(any())
    }

    @Test
    fun `reorder should handle empty positions map`() {
        categoryService.reorder(emptyMap())

        verifyNoInteractions(categoryRepository)
    }

    // getStats() tests
    @Test
    fun `getStats should return statistics for existing category`() {
        val category = createCategory(id = 1L, text = "Test Category")
        val activeSubCategory = SubCategory(text = "Active Sub", description = "desc", position = 1, route = "active", status = true)
        activeSubCategory.category = category
        val inactiveSubCategory = SubCategory(text = "Inactive Sub", description = "desc", position = 2, route = "inactive", status = false)
        inactiveSubCategory.category = category
        val activeProduct = Product(title = "Active Product", route = "active-product", price = java.math.BigDecimal("100.0"), status = true)
        activeProduct.category = category
        val inactiveProduct = Product(title = "Inactive Product", route = "inactive-product", price = java.math.BigDecimal("50.0"), status = false)
        inactiveProduct.category = category
        
        category.subCategories.add(activeSubCategory)
        category.subCategories.add(inactiveSubCategory)
        category.products.add(activeProduct)
        category.products.add(inactiveProduct)

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        val result = categoryService.getStats(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Test Category", result?.name)
        assertEquals(2, result?.subcategoriesCount)
        assertEquals(2, result?.productsCount)
        assertEquals(1, result?.activeSubcategoriesCount)
        assertEquals(1, result?.activeProductsCount)
        verify(categoryRepository).findById(1L)
    }

    @Test
    fun `getStats should return null when category does not exist`() {
        `when`(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        val result = categoryService.getStats(999L)

        assertNull(result)
        verify(categoryRepository).findById(999L)
    }

    @Test
    fun `getStats should return zero counts for category with no subcategories or products`() {
        val category = createCategory(id = 1L, text = "Empty Category")

        `when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        val result = categoryService.getStats(1L)

        assertNotNull(result)
        assertEquals(0, result?.subcategoriesCount)
        assertEquals(0, result?.productsCount)
        assertEquals(0, result?.activeSubcategoriesCount)
        assertEquals(0, result?.activeProductsCount)
        verify(categoryRepository).findById(1L)
    }
}
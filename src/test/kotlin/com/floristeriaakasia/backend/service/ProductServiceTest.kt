package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    private lateinit var productRepository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var subCategoryRepository: SubcategoryRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var productImageService: ProductImageService
    private lateinit var productVariantRepository: ProductVariantRepository
    private lateinit var productDescriptionRepository: ProductDescriptionRepository
    private lateinit var seoService: ProductSeoService
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mock(ProductRepository::class.java)
        categoryRepository = mock(CategoryRepository::class.java)
        subCategoryRepository = mock(SubcategoryRepository::class.java)
        tagRepository = mock(TagRepository::class.java)
        productImageService = mock(ProductImageService::class.java)
        productVariantRepository = mock(ProductVariantRepository::class.java)
        productDescriptionRepository = mock(ProductDescriptionRepository::class.java)
        seoService = mock(ProductSeoService::class.java)
        reviewRepository = mock(ReviewRepository::class.java)

        productService = ProductService(
            productRepository,
            categoryRepository,
            subCategoryRepository,
            tagRepository,
            productImageService,
            productVariantRepository,
            productDescriptionRepository,
            seoService,
            reviewRepository
        )
    }

    @Test
    fun `deleteById should delete images, SEO data, and product`() {
        // Given
        val productId = 1L
        val category = Category(text = "Test Category", route = "test-category", description = "Test")
        val subCategory = SubCategory(text = "Test SubCategory", route = "test-subcategory")
        subCategory.category = category
        
        val product = Product(
            title = "Test Product",
            route = "test-product",
            price = BigDecimal("100.00"),
            stockStatus = "In Stock",
            seasonal = false,
            featured = false,
            facebookUrl = "",
            instagramUrl = "",
            status = true
        )
        product.category = category
        product.subCategory = subCategory

        `when`(productRepository.findById(productId)).thenReturn(Optional.of(product))

        // When
        productService.deleteById(productId)

        // Then
        verify(productImageService).deleteAllImages(product)
        verify(seoService).deleteProductSeo(product)
        verify(productRepository).delete(product)
    }

    @Test
    fun `deleteById should throw exception when product not found`() {
        // Given
        val productId = 999L
        `when`(productRepository.findById(productId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<ResourceNotFoundException> {
            productService.deleteById(productId)
        }

        verifyNoInteractions(productImageService)
        verifyNoInteractions(seoService)
        verify(productRepository, never()).delete(any(Product::class.java))
    }
}

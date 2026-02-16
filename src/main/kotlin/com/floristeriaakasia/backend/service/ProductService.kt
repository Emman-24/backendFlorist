package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.dto.ProductCreateRequest
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import com.floristeriaakasia.backend.util.LoggingUtils.logFailure
import com.floristeriaakasia.backend.util.LoggingUtils.logSuccess
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubcategoryRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findAll(): List<Product> = productRepository.findAll()

    @Transactional(readOnly = true)
    fun findAllActive(): List<Product> = productRepository.findByStatus(true)

    @Transactional(readOnly = true)
    fun findAllWithFilters(
        categoryId: Long? = null,
        subcategoryId: Long? = null,
        featured: Boolean? = null,
        seasonal: Boolean? = null,
        pageable: Pageable
    ): Page<Product> {
        return productRepository.findWithFilters(
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            featured = featured,
            seasonal = seasonal,
            pageable = pageable
        )
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["products"], key = "#id")
    fun findById(id: Long): Product? = productRepository.findByIdOrNull(id)

    @Transactional(readOnly = true)
    @Cacheable(value = ["productBySlug"], key = "#slug")
    fun findBySlug(slug: String): Product? = productRepository.findBySlug(slug)

    @Transactional
    fun create(
        request: ProductCreateRequest
    ): Product {

        val startTime = System.currentTimeMillis()

        try {

            logger.info(
                "Creating product",
                kv("slug", request.slug),
                kv("title", request.title),
                kv("categoryId", request.categoryId),
                kv("price", request.price)
            )

            validateCategoryAndSubcategory(request.categoryId, request.subcategoryId)

            val product = Product(
                title = request.title,
                slug = request.slug,
                price = request.price,
                stockStatus = request.stockStatus,
                seasonal = request.seasonal,
                featured = request.featured,
                status = request.status
            ).apply {
                category = categoryRepository.findByIdOrNull(request.categoryId)
                    ?: throw IllegalArgumentException("Category not found")
                subCategory = subCategoryRepository.findByIdOrNull(request.subcategoryId)
                    ?: throw IllegalArgumentException("Subcategory not found")
            }
            val saved = productRepository.save(product)

            val duration = System.currentTimeMillis() - startTime
            logger.logSuccess(
                "Product created",
                "productId" to saved.id,
                "slug" to saved.slug,
                "durationMs" to duration
            )

            return saved

        } catch (e: Exception) {
            logger.logFailure(
                "Product creation",
                e,
                "slug" to request.slug,
                "categoryId" to request.categoryId
            )
            throw e
        }

    }

    @Transactional
    @CacheEvict(value = ["products", "productBySlug"], allEntries = true)
    fun update(
        id: Long,
        request: ProductCreateRequest
    ): Product {

        val startTime = System.currentTimeMillis()

        try {
            logger.info("Updating product",
                kv("productId", id),
                kv("newSlug", request.slug),
                kv("newPrice", request.price)
            )

            val product = findByIdOrThrow(id)
            val oldSlug = product.slug
            val oldPrice = product.price


            product.apply {
                title = request.title
                slug = request.slug
                price = request.price
                stockStatus = request.stockStatus
                seasonal = request.seasonal
                featured = request.featured
                status = request.status

                if (category.id != request.categoryId) {
                    category = categoryRepository.findByIdOrNull(request.categoryId)
                        ?: throw IllegalArgumentException("Category not found")
                }

                if (subCategory.id != request.subcategoryId) {
                    subCategory = subCategoryRepository.findByIdOrNull(request.subcategoryId)
                        ?: throw IllegalArgumentException("Subcategory not found")
                }
            }

            val saved = productRepository.save(product)

            val duration = System.currentTimeMillis() - startTime
            logger.logSuccess("Product updated",
                "productId" to saved.id,
                "oldSlug" to oldSlug,
                "oldPrice" to oldPrice,
                "newSlug" to saved.slug,
                "newPrice" to saved.price,
                "durationMs" to duration
            )
            return saved
        } catch (e: Exception) {
            logger.logFailure(
                "Product update",
                e,
                "productId" to id
            )
            throw e
        }
    }

    @Transactional
    @CacheEvict(value = ["products", "productBySlug"], allEntries = true)
    fun delete(id: Long) {
        val product = findByIdOrThrow(id)
        productRepository.delete(product)
    }

    @Transactional
    fun toggleStatus(id: Long): Product {
        val product = findByIdOrThrow(id)
        product.status = !product.status
        return productRepository.save(product)
    }

    @Transactional
    fun incrementViews(id: Long) {
        productRepository.incrementViews(id)
    }

    private fun findByIdOrThrow(id: Long): Product =
        productRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Product with id $id not found")

    private fun validateCategoryAndSubcategory(categoryId: Long, subcategoryId: Long) {
        val subcategory = subCategoryRepository.findByIdOrNull(subcategoryId)
            ?: throw IllegalArgumentException("Subcategory not found")

        if (subcategory.category.id != categoryId) {
            throw IllegalArgumentException("Subcategory does not belong to category")
        }

    }

}

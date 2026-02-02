package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.dto.ProductCreateRequest
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        val allProducts = productRepository.findByStatus(true)

        var filtered = allProducts.filter { it.status }

        categoryId?.let { catId ->
            filtered = filtered.filter { it.category.id == catId }
        }

        subcategoryId?.let { subId ->
            filtered = filtered.filter { it.subCategory.id == subId }
        }

        featured?.let { isFeatured ->
            filtered = filtered.filter { it.featured == isFeatured }
        }

        seasonal?.let { isSeasonal ->
            filtered = filtered.filter { it.seasonal == isSeasonal }
        }

        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, filtered.size)
        val pageContent = if (start < filtered.size) {
            filtered.subList(start, end)
        } else {
            emptyList()
        }

        return PageImpl(pageContent, pageable, filtered.size.toLong())
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Product? = productRepository.findByIdOrNull(id)

    @Transactional(readOnly = true)
    fun findBySlug(slug: String): Product? = productRepository.findBySlug(slug)

    @Transactional
    fun create(
        request: ProductCreateRequest
    ): Product {
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
        return productRepository.save(product)
    }

    @Transactional
    fun update(
        id: Long,
        request: ProductCreateRequest
    ): Product {
        val product = findByIdOrThrow(id)

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
        return productRepository.save(product)
    }

    @Transactional
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
        productRepository.findByIdOrNull(id)?.let { product ->
            product.views++
            productRepository.save(product)
        }
    }

    private fun findByIdOrThrow(id: Long): Product =
        productRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Product with id $id not found")

    private fun validateCategoryAndSubcategory(categoryId: Long, subcategoryId: Long) {
        val category =
            categoryRepository.findByIdOrNull(categoryId) ?: throw IllegalArgumentException("Category not found")
        val subcategory = subCategoryRepository.findByIdOrNull(subcategoryId)
            ?: throw IllegalArgumentException("Subcategory not found")

        if (subcategory.category.id != categoryId) {
            throw IllegalArgumentException("Subcategory does not belong to category")
        }

    }

}

package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.dto.ProductCreateRequest
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
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
    fun findById(id: Long): Product? = productRepository.findByIdOrNull(id)

    @Transactional(readOnly = true)
    fun findBySlug(slug: String): Product? = productRepository.findBySlug(slug)

    @Transactional
    fun create(request: ProductCreateRequest): Product {
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
        val category = categoryRepository.findByIdOrNull(categoryId) ?: throw IllegalArgumentException("Category not found")
        val subcategory = subCategoryRepository.findByIdOrNull(subcategoryId) ?: throw IllegalArgumentException("Subcategory not found")

        if (subcategory.category.id != categoryId) {
            throw IllegalArgumentException("Subcategory does not belong to category")
        }

    }

//    @Transactional
//    fun findByCategory(categoryId: Long): List<Product> {
//        val category = categoryRepository.findByIdOrNull(categoryId) ?: return emptyList()
//        return productRepository.findByCategoryAndStatus(category, true)
//    }
//
//    @Transactional(readOnly = true)
//    fun findBySubCategory(subCategoryId: Long): List<Product> {
//        val subCategory = subCategoryRepository.findByIdOrNull(subCategoryId) ?: return emptyList()
//        return productRepository.findBySubCategoryAndStatus(subCategory, true)
//    }
//
//    @Transactional(readOnly = true)
//    fun findFeatured(): List<Product> = productRepository.findByFeaturedTrueAndStatusOrderByCreatedAtDesc(true)
//
//
//    @Transactional()
//    fun save(
//        product: Product,
//        tagIds: List<Long>? = null,
//        generateSeo: Boolean = true
//    ): Product {
//        validateCategoryAndSubcategory(product)
//
//        val savedProduct = productRepository.save(product)
//
//        if (!tagIds.isNullOrEmpty()) {
//            assignTags(savedProduct, tagIds)
//        }
//
//        if (generateSeo) {
//            seoService.createOrUpdateProductSeo(savedProduct)
//        }
//        return savedProduct
//    }
//
//
//    @Transactional
//    fun deleteById(id: Long) {
//        val product = findByIdOrThrow(id)
//        productImageService.deleteAllImages(product)
//        seoService.deleteProductSeo(product)
//        productRepository.delete(product)
//    }
//
//    @Transactional
//    fun toggleFeatured(id: Long): Product {
//        val product = findByIdOrThrow(id)
//        product.featured = !product.featured
//        return productRepository.save(product)
//    }
//
//
//    @Transactional
//    fun uploadImage(
//        productId: Long,
//        file: MultipartFile,
//        altText: String? = null,
//        isPrimary: Boolean = false,
//        seasonal: Boolean = false
//    ): ProductGallery {
//        val product = findByIdOrThrow(productId)
//        return productImageService.uploadImage(product, file, altText, isPrimary, seasonal)
//    }
//
//    @Transactional
//    fun deleteImage(imageId: Long) {
//        productImageService.deleteImage(imageId)
//    }
//
//    fun setPrimaryImage(imageId: Long) {
//        productImageService.setPrimaryImage(imageId)
//    }
//
//    @Transactional
//    fun addVariant(
//        productId: Long,
//        variantType: String,
//        name: String,
//        priceAdjustment: BigDecimal,
//        description: String?
//    ): ProductVariant {
//        val product = findByIdOrThrow(productId)
//
//        val variant = ProductVariant(
//            variantType = variantType,
//            name = name,
//            priceAdjustment = priceAdjustment,
//            description = description ?: "",
//            position = product.variants.size,
//            available = true,
//            status = true
//        ).apply {
//            this.product = product
//        }
//        return productVariantRepository.save(variant)
//    }
//
//    fun deleteVariant(variantId: Long) {
//        val variant = productVariantRepository.findByIdOrNull(variantId)
//            ?: throw ResourceNotFoundException("Variant with id $variantId not found")
//        productVariantRepository.delete(variant)
//    }
//
//    @Transactional
//    fun addDescription(
//        productId: Long,
//        paragraph: String,
//        position: Int?
//    ): ProductDescription {
//        val product = findByIdOrThrow(productId)
//
//        val finalPosition = position ?: product.descriptions.size
//
//        val description = ProductDescription(
//            paragraph = paragraph,
//            position = finalPosition,
//            product = product
//        )
//        product.descriptions.add(description)
//        productRepository.save(product)
//
//        return description
//    }
//
//    fun updateDescription(
//        descriptionId: Long,
//        paragraph: String,
//        position: Int?
//    ): ProductDescription {
//        val description = productDescriptionRepository.findById(descriptionId)
//            .orElseThrow { ResourceNotFoundException("Description with id $descriptionId not found") }
//
//        description.paragraph = paragraph
//        position?.let { description.position = it }
//
//        return productDescriptionRepository.save(description)
//    }
//
//    @Transactional
//    fun deleteDescription(descriptionId: Long) {
//        val description = productDescriptionRepository.findById(descriptionId)
//            .orElseThrow { ResourceNotFoundException("Description with id $descriptionId not found") }
//
//        productDescriptionRepository.delete(description)
//    }
//
//
//    @Transactional
//    fun reorderDescriptions(
//        productId: Long,
//        descriptionIds: List<Long>
//    ) {
//        val product = findByIdOrThrow(productId)
//        val descriptions = product.descriptions
//
//        descriptionIds.forEachIndexed { index, descId ->
//            descriptions.find { it.id == descId }?.let { it.position = index }
//        }
//
//        productRepository.save(product)
//    }
//
//
//    @Transactional(readOnly = true)
//    fun getProductWithDetails(id: Long): ProductDetailsDTO? {
//        val product = productRepository.findByIdOrNull(id) ?: return null
//
//        val gallery = productImageService.getProductImages(product)
//        val reviews = reviewRepository.findApprovedByProductId(id)
//        val avgRating = reviewRepository.getAverageRatingByProductId(id) ?: 0.0
//
//        return ProductDetailsDTO(
//            product = product,
//            gallery = gallery,
//            variants = product.variants.sortedBy { it.position },
//            descriptions = product.descriptions.sortedBy { it.position },
//            tags = product.tags.toList(),
//            reviews = reviews,
//            averageRating = avgRating,
//            reviewCount = reviews.size.toLong()
//        )
//    }
//
//
//    @Transactional(readOnly = true)
//    fun getStats(): ProductStats {
//        return ProductStats(
//            total = productRepository.count().toInt(),
//            active = productRepository.countByStatus(true),
//            featured = productRepository.countByFeatured(true),
//            seasonal = productRepository.countBySeasonal(true),
//            totalViews = productRepository.sumViews() ?: 0,
//            averagePrice = productRepository.averagePrice() ?: BigDecimal.ZERO
//        )
//    }
//
//
//    private fun assignTags(
//        product: Product,
//        tagIds: List<Long>
//    ) {
//        val tags = tagRepository.findAllById(tagIds)
//        product.tags.clear()
//        product.tags.addAll(tags)
//    }
}

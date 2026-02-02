package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SeoUrl
import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SeoUrlRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.antlr.v4.runtime.misc.Triple
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeoUrlService(
    private val seoUrlRepository: SeoUrlRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository,

    @Value("\${app.base-url:https://www.floristeriaakasia.com.co}")
    private val baseUrl: String,
    repository: SubcategoryRepository
) {

    fun generateProductFullPath(product: Product): String {
        val categoryRoute = product.category.route
        val subCategoryRoute = product.subCategory.route
        val productRoute = product.slug
        return "/productos/$categoryRoute/$subCategoryRoute/$productRoute"
    }

    fun generateCategoryFullPath(category: Category): String {
        return "/productos/${category.route}"
    }

    fun generateSubCategoryFullPath(subCategory: SubCategory): String {
        val categoryRoute = subCategory.category.route
        return "/productos/$categoryRoute/${subCategory.route}"
    }

    @Transactional
    fun createOrUpdateProductUrl(product: Product): SeoUrl {
        val newFullPath = generateProductFullPath(product)
        val existingSeoUrl = seoUrlRepository.findByEntityTypeAndEntityId("product", product.id)

        return if (existingSeoUrl != null) {
            if (existingSeoUrl.fullPath != newFullPath) {

                existingSeoUrl.fullPath = newFullPath
                existingSeoUrl.slug = product.slug
                existingSeoUrl.canonicalUrl = "$baseUrl$newFullPath"
            }
            seoUrlRepository.save(existingSeoUrl)
        } else {
            seoUrlRepository.save(
                SeoUrl(
                    entityType = "product",
                    entityId = product.id!!,
                    slug = product.slug,
                    fullPath = newFullPath,
                    canonicalUrl = "$baseUrl$newFullPath"
                )
            )
        }
    }

    @Transactional
    fun createOrUpdateSubCategoryUrl(subCategory: SubCategory): SeoUrl {
        val newFullPath = generateSubCategoryFullPath(subCategory)
        val existingSeoUrl = seoUrlRepository.findByEntityTypeAndEntityId("subCategory", subCategory.id)

        return if (existingSeoUrl != null) {
            if (existingSeoUrl.fullPath != newFullPath) {

                existingSeoUrl.fullPath = newFullPath
                existingSeoUrl.slug = subCategory.route
                existingSeoUrl.canonicalUrl = "$baseUrl$newFullPath"

            }
            seoUrlRepository.save(existingSeoUrl)
        } else {
            seoUrlRepository.save(
                SeoUrl(
                    entityType = "subcategory",
                    entityId = subCategory.id!!,
                    slug = subCategory.route,
                    fullPath = newFullPath,
                    canonicalUrl = "$baseUrl$newFullPath"
                )
            )
        }
    }

    @Transactional
    fun createOrUpdateCategoryUrl(category: Category): SeoUrl {
        val newFullPath = generateCategoryFullPath(category)
        val existingSeoUrl = seoUrlRepository.findByEntityTypeAndEntityId("category", category.id)

        return if (existingSeoUrl != null) {
            if (existingSeoUrl.fullPath != newFullPath) {
                existingSeoUrl.fullPath = newFullPath
                existingSeoUrl.slug = category.route
                existingSeoUrl.canonicalUrl = "$baseUrl$newFullPath"
            }
            seoUrlRepository.save(existingSeoUrl)
        } else {
            seoUrlRepository.save(
                SeoUrl(
                    entityType = "category",
                    entityId = category.id!!,
                    slug = category.route,
                    fullPath = newFullPath,
                    canonicalUrl = "$baseUrl$newFullPath"
                )
            )
        }
    }

    @Transactional
    fun deleteProductUrl(product: Product) {
        seoUrlRepository.findByEntityTypeAndEntityId("product", product.id)?.let {
            seoUrlRepository.delete(it)
        }
    }

    @Transactional(readOnly = true)
    fun resolveUrl(path: String): Triple<String, Long, Boolean>? {
        seoUrlRepository.findByFullPath(path)?.let {
            return Triple(it.entityType, it.entityId, false)
        }

        return null
    }

    @Transactional
    fun generateAllCategoryUrls() {
        categoryRepository.findAll().forEach { category ->
            createOrUpdateCategoryUrl(category)
        }
    }

    @Transactional
    fun generateAllSubCategoryUrls() {
        subcategoryRepository.findAll().forEach { subCategory ->
            createOrUpdateSubCategoryUrl(subCategory)
        }
    }

    @Transactional
    fun generateAllProductUrls() {
        productRepository.findAll().forEach { product ->
            createOrUpdateProductUrl(product)
        }
    }

}
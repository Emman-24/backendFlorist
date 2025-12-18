package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SeoUrl
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SeoUrlRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SeoUrlService(
    private val seoUrlRepository: SeoUrlRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubcategoryRepository
) {
    fun generateProductFullPath(product: Product): String {
        val categoryRoute = product.category.route
        val subCategoryRoute = product.subCategory.route
        val productRoute = product.route
        return "/productos/$categoryRoute/$subCategoryRoute/$productRoute"
    }

    fun createOrUpdateProductUrl(product: Product): SeoUrl {
        val fullPath = generateProductFullPath(product)
        val existingSeoUrl: SeoUrl? = seoUrlRepository.findByEntityTypeAndEntityId("product", product.id)
        return if (existingSeoUrl != null) {
            if (existingSeoUrl.fullPath != fullPath) {

                val oldPaths = existingSeoUrl.redirectFrom.split(",").toMutableList()
                oldPaths.add(existingSeoUrl.fullPath)
                existingSeoUrl.redirectFrom = oldPaths.joinToString(",")
                existingSeoUrl.fullPath = fullPath
                existingSeoUrl.slug = product.route

            }
            seoUrlRepository.save(existingSeoUrl)

        } else {
            val newSeoUrl = SeoUrl(
                entityType = "product",
                entityId = product.id!!,
                slug = product.route,
                fullPath = fullPath,
                canonicalUrl = "https://www.floristeriaakasia.com.co$fullPath"
            )
            seoUrlRepository.save(newSeoUrl)
        }
    }

    fun resolveUrl(path: String): Pair<String, Long>? {
        val seoUrl = seoUrlRepository.findByFullPath(path)
            ?: seoUrlRepository.findByRedirectFrom(path).firstOrNull()
        return seoUrl?.let { Pair(it.entityType, it.entityId) }
    }

    fun generateCategoryUrls() {
        val categories = categoryRepository.findAll()
        categories.forEach { category ->
            val fullPath = "/productos/${category.route}"
            val existing = seoUrlRepository.findByEntityTypeAndEntityId("category", category.id)
            if (existing == null) {
                seoUrlRepository.save(
                    SeoUrl(
                        entityType = "category",
                        entityId = category.id!!,
                        slug = category.route,
                        fullPath = fullPath,
                        canonicalUrl = "https://www.floristeriaakasia.com.co$fullPath"
                    )
                )
            }
        }
    }

}
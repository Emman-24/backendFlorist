package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val seoUrlService: SeoUrlService
) {

    @Transactional(readOnly = true)
    fun findAll(): List<Category> {
        return categoryRepository.findAll().sortedBy { it.position }
    }

    @Transactional(readOnly = true)
    fun findAllActive(): List<Category> {
        return categoryRepository.findByStatusOrderByPositionAsc(true)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Category? {
        return categoryRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun findByRoute(route: String): Category? {
        return categoryRepository.findByRoute(route)
    }

    @Transactional
    fun save(category: Category): Category {
        val saved = categoryRepository.save(category)
        seoUrlService.createOrUpdateCategoryUrl(saved)
        return saved
    }

    @Transactional
    fun update(
        id: Long,
        category: Category
    ): Category {
        val existing = categoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Category with id $id not found")

        existing.apply {
            text = category.text
            route = category.route
            description = category.description
            position = category.position
            status = category.status
        }
        val updated = categoryRepository.save(existing)
        seoUrlService.createOrUpdateCategoryUrl(updated)
        return updated
    }

    @Transactional
    fun deleteById(id: Long) {
        val category =
            categoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Category with id $id not found")
        if (category.subCategories.isNotEmpty() || category.products.isNotEmpty()) {
            throw IllegalStateException(
                "Cannot delete category with ${category.subCategories.size} subcategories " +
                        "and ${category.products.size} products"
            )
        }

        categoryRepository.delete(category)
    }

    @Transactional
    fun toggleStatus(id: Long): Category {
        val category = categoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Category with id $id not found")
        category.status = !category.status
        return categoryRepository.save(category)
    }

    @Transactional
    fun reorder(positions: Map<Long, Int>) {
        positions.forEach { (id, position) ->
            categoryRepository.findByIdOrNull(id)?.let { category ->
                category.position = position
                categoryRepository.save(category)
            }
        }
    }

    @Transactional(readOnly = true)
    fun getStats(id: Long): CategoryStats? {
        val category = categoryRepository.findByIdOrNull(id) ?: return null
        return CategoryStats(
            id = category.id!!,
            name = category.text,
            subcategoriesCount = category.subCategories.size,
            productsCount = category.products.size,
            activeSubcategoriesCount = category.subCategories.count { it.status },
            activeProductsCount = category.products.count { it.status }
        )
    }
}

data class CategoryStats(
    val id: Long,
    val name: String,
    val subcategoriesCount: Int,
    val productsCount: Int,
    val activeSubcategoriesCount: Int,
    val activeProductsCount: Int
)
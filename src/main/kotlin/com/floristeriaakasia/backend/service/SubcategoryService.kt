package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.SubCategory
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubcategoryService(
    private val subcategoryRepository: SubcategoryRepository,
    private val categoryRepository: CategoryRepository,
    private val seoUrlService: SeoUrlService,
) {
    @Transactional(readOnly = true)
    fun findAll(): List<SubCategory> {
        return subcategoryRepository.findAll().sortedBy { it.position }
    }

    @Transactional(readOnly = true)
    fun findByCategoryId(categoryId: Long): List<SubCategory> {
        return subcategoryRepository.findByCategoryIdAndStatusOrderByPositionAsc(categoryId, true)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): SubCategory? {
        return subcategoryRepository.findByIdOrNull(id)
    }

    @Transactional
    fun save(subcategory: SubCategory): SubCategory {
        val saved = subcategoryRepository.save(subcategory)
        seoUrlService.createOrUpdateSubCategoryUrl(saved)
        return saved
    }

    @Transactional
    fun update(id: Long, subcategory: SubCategory): SubCategory {
        val existing = subcategoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Subcategory with id $id not found")

        existing.text = subcategory.text
        existing.route = subcategory.route
        existing.description = subcategory.description
        existing.category = subcategory.category
        existing.position = subcategory.position
        existing.status = subcategory.status

        val updated = subcategoryRepository.save(existing)
        seoUrlService.createOrUpdateSubCategoryUrl(updated)
        return updated
    }

    @Transactional
    fun deleteById(id: Long) {
        val subcategory = subcategoryRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Subcategory with id $id not found")
        subcategoryRepository.delete(subcategory)
    }

    @Transactional
    fun toggleStatus(id: Long) {
        val subcategory = subcategoryRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Subcategory with id $id not found")
        subcategory.status = !subcategory.status
        subcategoryRepository.save(subcategory)
    }

    @Transactional(readOnly = true)
    fun findByRoute(route: String): SubCategory? {
        return subcategoryRepository.findByRoute(route)
    }
}
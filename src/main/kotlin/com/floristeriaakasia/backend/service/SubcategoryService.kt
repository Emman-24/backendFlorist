package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.dto.subcategory.SubCategoryCreateRequest
import com.floristeriaakasia.backend.model.dto.subcategory.SubcategoryMapper
import com.floristeriaakasia.backend.model.dto.subcategory.SubcategoryResponse
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubcategoryService(
    private val subcategoryRepository: SubcategoryRepository,
    private val categoryRepository: CategoryRepository,
    private val mapper: SubcategoryMapper
) {
    @Transactional(readOnly = true)
    fun findAll(): List<SubcategoryResponse> {
        return subcategoryRepository.findAll().map(mapper::toResponse)
    }

    fun create(request: SubCategoryCreateRequest): SubcategoryResponse {
        val parentCategory = categoryRepository.findById(request.categoryId)
        val subCategory = mapper.toEntity(request, parentCategory.get())
        val savedSubCategory = subcategoryRepository.save(subCategory)
        return mapper.toResponse(savedSubCategory)
    }

    @Transactional(readOnly = true)
    fun findRequestById(id: Long): SubCategoryCreateRequest {
        val entity = subcategoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Subcategory with id $id not found")
        return mapper.toCreateRequest(entity)
    }

    fun update(id: Long, request: SubCategoryCreateRequest): SubcategoryResponse {
        val existingSubcategory = subcategoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Subcategory with id $id not found")

        existingSubcategory.text = request.text
        existingSubcategory.route = request.route
        existingSubcategory.status = request.status


        val updated = subcategoryRepository.save(existingSubcategory)
        return mapper.toResponse(updated)
    }

    fun deleteById(id: Long) {
        if (!subcategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("Subcategory with $id not found")
        }
        subcategoryRepository.deleteById(id)
    }
}
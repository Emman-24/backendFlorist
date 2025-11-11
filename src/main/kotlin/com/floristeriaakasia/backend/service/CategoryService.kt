package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.dto.category.CategoryMapper
import com.floristeriaakasia.backend.model.dto.category.CategoryRequest
import com.floristeriaakasia.backend.model.dto.category.CategoryResponse
import com.floristeriaakasia.backend.repository.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val mapper: CategoryMapper
) {
    @Transactional(readOnly = true)
    fun findAll(): List<CategoryResponse> {
        return categoryRepository.findAll().map(mapper::toResponse)
    }

    @Transactional(readOnly = true)
     fun findRequestById(id: Long): CategoryRequest {
        return categoryRepository.findByIdOrNull(id)
            ?.let {
                CategoryRequest(text = it.text, route = it.route, status = it.status)
            }
            ?: throw ResourceNotFoundException("Category with id $id not found")
    }

    fun create(request: CategoryRequest): CategoryResponse {
        val category = mapper.toEntity(request)
        val savedCategory = categoryRepository.save(category)
        return mapper.toResponse(savedCategory)
    }

    fun update(id: Long, request: CategoryRequest): CategoryResponse {
        val existingCategory = categoryRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Category with id $id not found")
        mapper.updateEntityFromRequest(request, existingCategory)
        val updatedCategory = categoryRepository.save(existingCategory)
        return mapper.toResponse(updatedCategory)
    }

    fun deleteById(id: Long) {
        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException("Category with id $id not found")
        }
        categoryRepository.deleteById(id)
    }


}
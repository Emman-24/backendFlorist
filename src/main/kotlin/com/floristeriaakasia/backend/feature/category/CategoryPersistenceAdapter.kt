package com.floristeriaakasia.backend.feature.category

import org.springframework.stereotype.Component

@Component
class CategoryPersistenceAdapter(
    private val repository: CategoryRepository
): LoadCategoryPort {
    override fun loadAllByIds(ids: Set<Long>): List<Category> {
        return repository.findAllByIdInAndStatusTrue(ids)
    }
}
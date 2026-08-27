package com.floristeriaakasia.backend.feature.category.infrastructure.api

interface LoadCategoryPort {
    fun loadAllByIds(ids: Set<Long>): List<Category>
}
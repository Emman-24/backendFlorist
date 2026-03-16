package com.floristeriaakasia.backend.feature.category

interface LoadCategoryPort {
    fun loadAllByIds(ids: Set<Long>): List<Category>
}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubcategoryRepository : JpaRepository<SubCategory, Long> {
    fun findByRoute(route: String): SubCategory?
    fun findByCategoryAndStatus(category: Category, status: Boolean): List<SubCategory>
    fun findByCategoryIdAndStatusOrderByPositionAsc(categoryId: Long, status: Boolean): List<SubCategory>
}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByStatus(status: Boolean): List<Category>
    fun findByStatusOrderByPositionAsc(status: Boolean): List<Category>
    fun findByRoute(route: String): Category?
}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun existsByText(name: String): Boolean
    fun existsByRoute(route: String): Boolean
    fun findByRoute(route: String): Category?
}
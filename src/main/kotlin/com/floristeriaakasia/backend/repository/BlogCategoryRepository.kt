package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.BlogCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BlogCategoryRepository : JpaRepository<BlogCategory, Long> {
    fun findBySlug(slug: String): BlogCategory?
    fun findByStatusOrderByPositionAsc(status: Boolean): List<BlogCategory>
}
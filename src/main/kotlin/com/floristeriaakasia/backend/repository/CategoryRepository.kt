package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findBySlug(slug: String): Category?

    @Query("SELECT c FROM Category c WHERE c.path LIKE :pathPrefix ORDER BY c.depth ASC, c.displayOrder ASC")
    fun findSubtree(@Param("pathPrefix") pathPrefix: String): List<Category>

    @Query("SELECT c FROM Category c ORDER BY c.depth ASC, c.displayOrder ASC")
    fun findFullTree(): List<Category>

}
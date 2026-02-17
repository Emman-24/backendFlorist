package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByStatus(status: Boolean): List<Category>

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.status = :status ORDER BY c.position ASC")
    fun findByStatusOrderByPositionAsc(@Param("status") status: Boolean): List<Category>

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.route = :route")
    fun findByRoute(@Param("route") route: String): Category?

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subCategories ORDER BY c.position ASC")
    fun findAllWithSubcategories(): List<Category>

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    fun findByIdWithSubcategories(@Param("id") id: Long): Category?
}
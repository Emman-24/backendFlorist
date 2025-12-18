package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.BlogPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BlogRepository : JpaRepository<BlogPost, Long> {
    fun findBySlug(slug: String): BlogPost?

    fun findByStatusOrderByPublishedAtDesc(status: String): List<BlogPost>

    @Query(
        """
        SELECT b FROM BlogPost b 
        WHERE b.status = 'published' 
        AND b.publishedAt IS NOT NULL
        ORDER BY b.publishedAt DESC
    """
    )
    fun findPublishedPosts(): List<BlogPost>

    @Query(
        """
        SELECT b FROM BlogPost b 
        JOIN b.categories c 
        WHERE c.id = :categoryId 
        AND b.status = 'published'
        ORDER BY b.publishedAt DESC
    """
    )
    fun findPublishedByCategoryId(@Param("categoryId") categoryId: Long): List<BlogPost>

    @Query(
        """
        SELECT b FROM BlogPost b 
        WHERE b.status = 'published' 
        AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(b.content) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY b.publishedAt DESC
    """
    )
    fun searchPublishedPosts(@Param("search") search: String): List<BlogPost>
}
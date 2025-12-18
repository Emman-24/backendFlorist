package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Faq
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FaqRepository: JpaRepository<Faq, Long> {
    fun findByStatusOrderByPositionAsc(status: Boolean): List<Faq>

    fun findByCategoryAndStatusOrderByPositionAsc(category: String, status: Boolean): List<Faq>

    @Query("""
        SELECT f FROM Faq f 
        WHERE f.status = :status 
        AND (LOWER(f.question) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.answer) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY f.position ASC
    """)
    fun searchFaqs(
        @Param("search") search: String,
        @Param("status") status: Int
    ): List<Faq>

    @Query("""
        SELECT f FROM Faq f 
        WHERE f.status = :status 
        ORDER BY f.views DESC
    """)
    fun findTopViewedFaqs(@Param("status") status: Int): List<Faq>
}
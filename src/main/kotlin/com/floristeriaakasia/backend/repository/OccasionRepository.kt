package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Occasion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OccasionRepository: JpaRepository<Occasion, Long> {
    fun findBySlug(slug: String): Occasion?

    fun findByStatusOrderByPositionAsc(status: Boolean): List<Occasion>

    @Query("""
        SELECT o FROM Occasion o 
        WHERE o.status = :status 
        AND (
            (o.dateSpecific BETWEEN :startDate AND :endDate)
            OR (o.dateRangeStart <= :endDate AND o.dateRangeEnd >= :startDate)
            OR o.isRecurring = true
        )
        ORDER BY o.position ASC
    """)
    fun findActiveOccasionsByDateRange(
        @Param("startDate") startDate: java.time.LocalDateTime,
        @Param("endDate") endDate: java.time.LocalDateTime,
        @Param("status") status: Int
    ): List<Occasion>
}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.SeoRedirect
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SeoRedirectRepository : JpaRepository<SeoRedirect, Long> {
    fun findByOldPathAndIsActiveTrue(oldPath: String): SeoRedirect?
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long): List<SeoRedirect>
    fun existsByOldPathAndIsActiveTrue(oldPath: String): Boolean


    @Query("""
        SELECT r FROM SeoRedirect r 
        WHERE r.isActive = true 
        AND (r.lastHitAt IS NULL OR r.lastHitAt < :cutoffDate)
    """)
    fun findStaleRedirects(@Param("cutoffDate") cutoffDate: LocalDateTime): List<SeoRedirect>

    @Query("SELECT r FROM SeoRedirect r WHERE r.isActive = true ORDER BY r.hitCount DESC")
    fun findMostUsed(): List<SeoRedirect>

}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.SeoUrl
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SeoUrlRepository : JpaRepository<SeoUrl, Long> {
    fun findBySlug(slug: String): SeoUrl?
    fun findByFullPath(fullPath: String): SeoUrl?
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long?): SeoUrl?
}
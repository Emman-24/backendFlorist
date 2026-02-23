package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.SeoMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SeoMetadataRepository: JpaRepository<SeoMetadata, Long> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: Long): SeoMetadata?
}
package com.floristeriaakasia.backend.feature.floralArrangment.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FloralArrangementRepository : JpaRepository<FloralArrangement, Long> {
    fun existsBySlug(slug: String): Boolean
}
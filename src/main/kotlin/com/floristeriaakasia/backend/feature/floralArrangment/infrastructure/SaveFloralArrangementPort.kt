package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement

interface SaveFloralArrangementPort {
    fun save(arrangement: FloralArrangement): Long
    fun existsBySlug(slug: String): Boolean
}
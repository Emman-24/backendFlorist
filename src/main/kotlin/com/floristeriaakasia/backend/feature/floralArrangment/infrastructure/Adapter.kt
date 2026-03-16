package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import org.springframework.stereotype.Component

@Component
class FloralArrangementPersistenceAdapter(
    private val floralArrangementRepo: FloralArrangementRepository
) : SaveFloralArrangementPort {

    override fun save(arrangement: FloralArrangement): Long =
        floralArrangementRepo.save(arrangement).id

    override fun existsBySlug(slug: String): Boolean =
        floralArrangementRepo.existsBySlug(slug)
}



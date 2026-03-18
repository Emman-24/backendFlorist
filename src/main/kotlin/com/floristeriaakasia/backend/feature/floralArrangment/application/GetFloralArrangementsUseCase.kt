package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementDetailDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementQuery
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementSummaryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetFloralArrangementsUseCase {
    fun execute(query: FloralArrangementQuery, pageable: Pageable): Page<FloralArrangementSummaryDto>
    fun executeById(id: Long): FloralArrangementDetailDto
}
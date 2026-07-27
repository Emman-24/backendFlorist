package com.floristeriaakasia.backend.feature.flowers

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FlowersRepository : JpaRepository<Flowers, Long> {
    fun findByFloralArrangementId(arrangementId: Long, pageable: Pageable): Page<Flowers>
}
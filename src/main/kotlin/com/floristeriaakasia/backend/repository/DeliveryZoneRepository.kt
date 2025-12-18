package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.DeliveryZone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryZoneRepository : JpaRepository<DeliveryZone, Long> {
    fun findByActiveTrueOrderByPositionAsc(): List<DeliveryZone>
    fun findByName(name: String): DeliveryZone?
}
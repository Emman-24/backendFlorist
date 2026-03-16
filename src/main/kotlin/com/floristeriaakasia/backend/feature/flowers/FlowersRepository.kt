package com.floristeriaakasia.backend.feature.flowers

import org.springframework.data.jpa.repository.JpaRepository

interface FlowersRepository : JpaRepository<Flowers, Long> {
}
package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Banner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BannerRepository : JpaRepository<Banner, Long> {
}
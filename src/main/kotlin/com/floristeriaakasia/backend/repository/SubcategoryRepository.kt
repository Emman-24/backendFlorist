package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubcategoryRepository : JpaRepository<SubCategory, Long> {
}
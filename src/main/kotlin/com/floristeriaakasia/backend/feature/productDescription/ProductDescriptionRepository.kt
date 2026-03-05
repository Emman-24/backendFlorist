package com.floristeriaakasia.backend.feature.productDescription

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductDescriptionRepository : JpaRepository<ProductDescription, Long>
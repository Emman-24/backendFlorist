package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductDescription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductDescriptionRepository: JpaRepository<ProductDescription, Long> {
    fun findByProductOrderByPositionAsc(product: Product): List<ProductDescription>
}
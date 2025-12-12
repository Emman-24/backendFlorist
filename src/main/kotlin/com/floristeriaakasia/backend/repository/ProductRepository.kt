package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByStatus(status: Boolean, pageable: Pageable): Page<Product>
}
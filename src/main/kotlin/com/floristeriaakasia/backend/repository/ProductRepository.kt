package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.StockStatus
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySlug(slug: String): Product?
    fun findByStatus(status: Boolean): List<Product>
}
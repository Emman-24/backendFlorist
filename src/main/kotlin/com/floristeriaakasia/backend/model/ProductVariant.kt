package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "product_variants", indexes = [
        Index(name = "idx_product_variants", columnList = "product_id, variant_type")
    ]
)
class ProductVariant(
    var variantType: String = "",
    var name: String = "",
    var priceAdjustment: BigDecimal = BigDecimal.ZERO,
    var description: String = "",
    var position: Int = 0,
    var available: Boolean = true,
    var status: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product = Product()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
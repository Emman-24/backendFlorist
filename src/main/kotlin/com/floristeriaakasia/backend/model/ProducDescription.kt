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

@Entity
@Table(
    name = "product_descriptions",
    indexes = [
        Index(name = "idx_product_desc_product", columnList = "product_id"),
        Index(name = "idx_product_desc_position", columnList = "product_id,position")
    ]
)
class ProductDescription(
    @Column(nullable = false, columnDefinition = "TEXT")
    var paragraph: String = "",

    @Column(nullable = false)
    var position: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

}
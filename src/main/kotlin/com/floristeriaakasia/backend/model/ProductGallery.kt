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
import java.time.Instant

@Entity
@Table(
    name = "product_gallery",
    indexes = [
        Index(name = "idx_product_gallery", columnList = "product_id, position"),
    ],
)
class ProductGallery(
    var originalName: String = "",
    var storedName: String = "",
    var mimeType: String = "",
    var size: Long = Long.MIN_VALUE,
    var altText: String = "",
    var isPrimary: Boolean = false,
    var position: Int = 0,
    var seasonal: Boolean = false,
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
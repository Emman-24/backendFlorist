package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.time.Instant

@Entity
@Table(
    name = "product_seo_content",
    indexes = [
        Index(name = "idx_product_seo_content", columnList = "product_id, content_type"),
    ],
)
class ProductSeoContent(
    var contentType: String = "",
    var title: String = "",
    var content: String = "",
    var position: Int = 0,
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
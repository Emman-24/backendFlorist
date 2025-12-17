package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "sub_category")
class SubCategory(
    var text: String = "",
    var route: String = "",
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category = Category()

    @OneToMany(mappedBy = "subCategory", cascade = [], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @CreationTimestamp
    val updatedAt: Instant? = null
}
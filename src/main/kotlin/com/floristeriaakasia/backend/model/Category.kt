package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "categories")
class Category(
    var text: String = "",
    var route: String = "",
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "category", cascade = [], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf()

    @OneToMany(
        mappedBy = "category",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var subCategories: MutableList<SubCategory> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @CreationTimestamp
    val updatedAt: Instant? = null
}

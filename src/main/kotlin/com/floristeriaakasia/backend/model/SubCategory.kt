package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "sub_category")
class SubCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @OneToMany(mappedBy = "subCategory", cascade = [], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf(),

    @field:NotBlank(message = "Subcategory name is required")
    @Column(nullable = false, unique = true)
    var text: String = "",

    @field:NotBlank(message = "Subcategory route is required")
    @Column(nullable = false, unique = true)
    var route: String = "",

    @Column(nullable = false)
    var status: Boolean = true,

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SubCategory
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
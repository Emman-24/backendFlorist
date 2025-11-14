package com.floristeriaakasia.backend.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import lombok.AllArgsConstructor
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToMany(mappedBy = "category", cascade = [], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf(),

    @OneToMany(
        mappedBy = "category",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var subCategories: MutableList<SubCategory> = mutableListOf(),

    @field:NotBlank(message = "Category name is required")
    @Column(nullable = false, unique = true)
    var text: String = "",

    @field:NotBlank(message = "Category route is required")
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
        other as Category
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

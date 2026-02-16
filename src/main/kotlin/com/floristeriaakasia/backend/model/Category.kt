package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener::class)
class Category(
    var text: String = "",
    var route: String = "",
    var description: String = "",
    var position: Int = 0,
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf()

    @OneToMany(
        mappedBy = "category",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var subCategories: MutableList<SubCategory> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
}

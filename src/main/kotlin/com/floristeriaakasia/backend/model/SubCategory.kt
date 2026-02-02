package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "sub_category")
@EntityListeners(AuditingEntityListener::class)
class SubCategory(
    var text: String = "",
    var description: String = "",
    var position: Int = 0,
    var route: String = "",
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    lateinit var category: Category

    @OneToMany(mappedBy = "subCategory", cascade = [CascadeType.ALL], orphanRemoval = false)
    var products: MutableList<Product> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
}
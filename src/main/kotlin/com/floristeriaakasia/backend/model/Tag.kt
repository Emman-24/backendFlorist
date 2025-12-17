package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "tags")
class Tag(
    var text: String = "",
    var route: String = "",
    var status: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToMany(mappedBy = "tags")
    var products: MutableList<Product> = mutableListOf()

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @CreationTimestamp
    val updatedAt: Instant? = null
}

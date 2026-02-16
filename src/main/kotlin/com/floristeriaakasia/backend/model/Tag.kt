package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener::class)
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
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
}

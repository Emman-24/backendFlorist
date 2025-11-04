package com.floristeriaakasia.backend.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "banners")
class Banner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var imagePath: String,

    @Column(nullable = false)
    var firstTitle: String,

    @Column(nullable = false)
    var secondTitle: String,

    @Column(nullable = false)
    var status: Boolean = true,

    @Column(length = 1024)
    var linkUrl: String? = null,

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Instant = Instant.now()
)

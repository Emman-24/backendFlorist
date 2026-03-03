package com.floristeriaakasia.backend.feature.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    val name: String,

    @Column(length = 255)
    val description: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
        const val MANAGER = "MANAGER"
    }
}
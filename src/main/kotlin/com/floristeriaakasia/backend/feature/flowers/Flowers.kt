package com.floristeriaakasia.backend.feature.flowers

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "flowers")
@EntityListeners(AuditingEntityListener::class)
data class Flowers(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var meaning: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floral_arrangement_id", nullable = false)
    var floralArrangement: FloralArrangement? = null,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    val updatedAt: Instant = Instant.now()
)

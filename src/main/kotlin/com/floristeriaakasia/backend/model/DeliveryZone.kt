package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "delivery_zones")
@EntityListeners(AuditingEntityListener::class)
class DeliveryZone(
    var name: String = "",
    var description: String = "",
    var polygon: String = "",
    var deliveryFee: BigDecimal = BigDecimal.ZERO,
    var minimumOrderValue: BigDecimal = BigDecimal.ZERO,
    var estimatedTimeMin: Int = 0,
    var estimatedTimeMax: Int = 0,
    var active: Boolean = true,
    var position: Int = 0
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
}
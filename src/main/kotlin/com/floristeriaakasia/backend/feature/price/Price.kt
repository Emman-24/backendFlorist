package com.floristeriaakasia.backend.feature.price

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "prices")
data class Price(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column
    var discountPrice: BigDecimal? = null,

    @Column(nullable = false)
    var currency: String = "COP",

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now(),
    )

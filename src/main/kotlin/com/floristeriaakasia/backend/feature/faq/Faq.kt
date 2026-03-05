package com.floristeriaakasia.backend.feature.faq

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "faqs")
@EntityListeners(AuditingEntityListener::class)
data class Faq(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var question: String,

    @Column(nullable = false)
    var answer: String,

    @Column
    var position: Int = 0,

    @Column
    var views: Int = 0,

    @Column
    var status: Boolean = true,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()

)
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
class Flowers(
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
    var createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Flowers) return false
        if (id != null && other.id != null) return id == other.id
        return name == other.name && meaning == other.meaning
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: (name.hashCode() * 31 + meaning.hashCode())
    }

    override fun toString(): String {
        return "Flowers(id=$id, name='$name', meaning='$meaning')"
    }
}

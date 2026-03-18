package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener::class)
class Tag(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val text: String,

    @Column(nullable = false)
    val route: String,

    @Column(nullable = false)
    val description: String,

    @ManyToMany(mappedBy = "tags")
    var floralArrangements: MutableList<FloralArrangement> = mutableListOf(),

    @Column(nullable = false)
    val status: Boolean = true,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    val updatedAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        if (id != null && other.id != null) return id == other.id
        return text == other.text && route == other.route
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: (text.hashCode() * 31 + route.hashCode())
    }

    override fun toString(): String {
        return "Tag(id=$id, text='$text', route='$route')"
    }
}
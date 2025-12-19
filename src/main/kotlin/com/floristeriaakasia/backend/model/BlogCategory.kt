package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "blog_categories")
@EntityListeners(AuditingEntityListener::class)
class BlogCategory(
    var name: String = "",
    var slug: String = "",
    var description: String = "",
    var position: Int = 0,
    var status: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToMany(mappedBy = "categories")
    val posts: MutableSet<BlogPost> = mutableSetOf()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
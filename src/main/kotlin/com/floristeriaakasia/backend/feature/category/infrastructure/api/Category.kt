package com.floristeriaakasia.backend.feature.category.infrastructure.api

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
import java.time.Instant

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener::class)
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(nullable = false, length = 3000)
    var path: String,

    @Column
    var parentId: Long? = null,

    @Column(nullable = false)
    var depth: Int,

    @Column(nullable = false)
    var displayOrder: Int = 0,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var status: Boolean = true,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    var updatedAt: Instant = Instant.now()

) {
    companion object {
        fun buildRootPath(id: Long): String = "/$id/"
        fun buildChildPath(parentPath: String, newId: Long): String = "${parentPath}$newId/"
        fun createRoot(name: String, slug: String, displayOrder: Int = 0, description: String): Category =
            Category(
                name = name,
                slug = slug,
                path = "",
                parentId = null,
                depth = 0,
                displayOrder = displayOrder,
                description = description
            )

        fun createChild(
            name: String,
            slug: String,
            parent: Category,
            displayOrder: Int = 0,
            description: String
        ): Category {
            require(parent.id != null) { "Parent must be persisted before creating children" }
            return Category(
                name = name,
                slug = slug,
                path = "",
                parentId = parent.id,
                depth = parent.depth + 1,
                displayOrder = displayOrder,
                description = description
            )
        }
    }

    fun descendantPathPattern(): String = "$path%"
    fun ancestorIds(): List<Long> {
        val segments = path.trim('/').split('/')
        val selfId = id?.toString()
        return segments
            .filter { it.isNotBlank() && it != selfId }
            .map { it.toLong() }
    }

    fun isDescendantOf(potentialAncestor: Category): Boolean =
        potentialAncestor.id?.let { path.contains("/${it}/") } ?: false

    fun buildMovedPath(newParentPath: String): String {
        require(id != null) { "Cannot build path for unpersisted category" }
        return "$newParentPath$id/"
    }

    fun isPathValid(): Boolean {
        if (!path.startsWith("/") || !path.endsWith("/")) return false
        val segments = path.trim('/').split('/')
        if (segments.isEmpty() || segments.any { it.isBlank() }) return false
        if (id != null && segments.last() != id.toString()) return false
        return true
    }
}
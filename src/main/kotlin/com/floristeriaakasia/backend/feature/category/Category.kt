package com.floristeriaakasia.backend.feature.category

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
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val slug: String,

    @Column(nullable = false, length = 3000)
    var path: String,

    @Column
    val parentId: Long? = null,

    @Column(nullable = false)
    val depth: Int,

    @Column(nullable = false)
    val displayOrder: Int = 0,

    @Column
    val description: String? = null,

    @Column(nullable = false)
    val status: Boolean = true,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    val createdAt: Instant = Instant.now(),

    @Column(nullable = true)
    @LastModifiedDate
    val updatedAt: Instant = Instant.now()

) {
    companion object{
        fun buildPath(parentPath: String?, nodeId: Long): String =
            if (parentPath == null) "/$nodeId/" else "$parentPath$nodeId/"
        fun buildRootPath(id: Long): String = "/$id/"
        fun buildChildPath(parentPath: String, newId: Long): String = "${parentPath}$newId/"
        fun createRoot(name: String, slug: String, displayOrder: Int = 0): Category =
            Category(
                name = name,
                slug = slug,
                path = "",
                parentId = null,
                depth = 0,
                displayOrder = displayOrder
            )

        fun createChild(
            name: String,
            slug: String,
            parent: Category,
            displayOrder: Int = 0
        ): Category {
            require(parent.id != null) { "Parent must be persisted before creating children" }
            return Category(
                name = name,
                slug = slug,
                path = "",
                parentId = parent.id,
                depth = parent.depth + 1,
                displayOrder = displayOrder
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
    fun isDescendantOf(potentialAncestor: Category): Boolean = potentialAncestor.id?.let { path.contains("/${it}/") } ?: false
    fun isRoot(): Boolean = parentId == null
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
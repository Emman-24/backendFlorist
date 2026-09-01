package com.floristeriaakasia.backend.feature.category.infrastructure.api

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val repository: CategoryRepository,
) {

    fun getById(id: Long): Category {
        return repository.findByIdOrNull(id) ?: throw IllegalArgumentException("Category not found")
    }

    fun getRoots() = repository.findFullTree()

    fun getFullTree() = repository.findFullTree()

    @Transactional
    fun createRoot(
        name: String,
        slug: String,
        displayOrder: Int = 0,
        description: String
    ): Category {
        val category = Category.createRoot(name, slug, displayOrder, description)
        val saved = repository.save(category)
        saved.path = Category.buildRootPath(saved.id!!)
        return repository.save(saved)
    }

    @Transactional
    fun createChild(
        name: String,
        slug: String,
        parentId: Long,
        displayOrder: Int = 0,
        description: String
    ): Category {
        val parent: Category = getById(parentId)
        val category: Category = Category.createChild(
            name = name,
            slug = slug,
            parent = parent,
            displayOrder = displayOrder,
            description = description
        )
        val saved: Category = repository.save(category)
        saved.path = Category.buildChildPath(parent.path, saved.id!!)
        return repository.save(saved)
    }

    fun buildTreeFromFlat(
        categories: List<Category>
    ): List<CategoryNode> {
        val nodeMap = categories.associateBy({ it.id!! }, { CategoryNode(it) })
        val roots = mutableListOf<CategoryNode>()

        categories.forEach { cat ->
            val node = nodeMap[cat.id!!]!!
            if (cat.parentId == null) {
                roots.add(node)
            } else {
                nodeMap[cat.parentId]?.children?.add(node)
            }
        }
        return roots.sortedBy { it.category.displayOrder }
    }

    @Transactional
    fun update(
        id: Long,
        name: String,
        slug: String,
        displayOrder: Int,
        description: String?
    ): Category {
        val existing = getById(id)
        existing.name = name
        existing.slug = slug
        existing.displayOrder = displayOrder
        existing.description = description
        return repository.save(existing)
    }

    @Transactional
    fun setStatus(
        id: Long,
        status: Boolean
    ): Category {
        val existing = getById(id)
        existing.status = status
        return repository.save(existing)
    }

    @Transactional
    fun move(id: Long, newParentId: Long?): Category {

        val category = getById(id)
        require(category.parentId != newParentId) { "Category is already under this parent" }

        val newParent = newParentId?.let { getById(it) }
        if (newParent != null) {
            require(newParent.id != category.id && !newParent.isDescendantOf(category)) {
                "Cannot move a category under its own descendant"
            }
        }

        val oldPath = category.path
        val oldDepth = category.depth
        val newDepth = (newParent?.depth ?: -1) + 1
        val newPath =
            if (newParent != null) category.buildMovedPath(newParent.path) else Category.buildRootPath(category.id!!)

        category.parentId = newParentId
        category.depth = newDepth
        category.path = newPath
        val movedCategory = repository.save(category)

        val depthDelta = newDepth - oldDepth
        val descendants = repository.findSubtree("$oldPath%")
            .filterNot { it.id == category.id }
            .map {
                it.path = newPath + it.path.removePrefix(oldPath)
                it.depth += depthDelta
                it
            }

        repository.saveAll(descendants)
        return movedCategory
    }

}
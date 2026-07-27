package com.floristeriaakasia.backend.feature.category

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
        displayOrder: Int = 0
    ): Category {
        val category = Category.createRoot(name, slug, displayOrder)
        val saved = repository.save(category)
        val withPath = saved.copy(path = Category.buildRootPath(saved.id!!))
        return repository.save(withPath)
    }

    @Transactional
    fun createChild(
        name: String,
        slug: String,
        parentId: Long,
        displayOrder: Int = 0
    ): Category {
        val parent = getById(parentId)
        val category = Category.createChild(
            name = name,
            slug = slug,
            parent = parent,
            displayOrder = displayOrder
        )
        val saved = repository.save(category)
        val withPath = saved.copy(path = Category.buildChildPath(parent.path, saved.id!!))
        return repository.save(withPath)
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
    ):Category {
        val existing = getById(id)
        return repository.save(existing.copy(name = name, slug = slug, displayOrder = displayOrder, description = description))
    }

    @Transactional
    fun setStatus(
        id: Long,
        status: Boolean
    ): Category {
        val existing = getById(id)
        return repository.save(existing.copy(status = status))
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
        val newDepth = (newParent?.depth ?: -1) + 1
        val newPath = if (newParent != null) category.buildMovedPath(newParent.path) else Category.buildRootPath(category.id!!)

        val movedCategory = repository.save(category.copy(parentId = newParentId, depth = newDepth, path = newPath))

        val depthDelta = newDepth - category.depth
        val descendants = repository.findSubtree(category.descendantPathPattern())
            .filterNot { it.id == category.id }
            .map { it.copy(path = newPath + it.path.removePrefix(oldPath), depth = it.depth + depthDelta) }

        repository.saveAll(descendants)
        return movedCategory
    }

}
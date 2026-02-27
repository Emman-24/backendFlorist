package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.dto.CategoryNode
import com.floristeriaakasia.backend.repository.CategoryRepository
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



}


package com.floristeriaakasia.backend.feature.category

import com.floristeriaakasia.backend.config.RepositoryTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryRepositoryTest : RepositoryTestBase() {
    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    private lateinit var bouquets: Category
    private lateinit var roses: Category
    private lateinit var tulips: Category
    private lateinit var redRoses: Category
    private lateinit var archived: Category

    @BeforeEach
    fun setUp() {
        bouquets = saveRoot(name = "Bouquets", slug = "bouquets")
        roses = saveChild(name = "Roses", slug = "roses", parent = bouquets, displayOrder = 1)
        tulips = saveChild(name = "Tulips", slug = "tulips", parent = bouquets, displayOrder = 0)
        redRoses = saveChild(name = "Red roses", slug = "red-roses", parent = roses)
        archived = saveRoot(name = "Archived", slug = "archived", status = false)
    }

    @Test
    fun `findBySlug returns the matching category`() {
        val category = categoryRepository.findBySlug("bouquets")

        assertNotNull(category)
        assertEquals("Bouquets", category.name)
        assertEquals(bouquets.id, category.id)
    }

    @Test
    fun `findBySlug returns inactive categories too`() {
        assertNotNull(categoryRepository.findBySlug("archived"))
    }

    @Test
    fun `findBySlug returns null when no category has that slug`() {
        assertNull(categoryRepository.findBySlug("does-not-exist"))
    }

    @Test
    fun `slug is unique`() {
        val duplicate = Category(
            name = "Another bouquets",
            slug = "bouquets",
            path = "",
            depth = 0
        )

        assertFailsWith<DataIntegrityViolationException> {
            categoryRepository.saveAndFlush(duplicate)
        }
    }

    @Test
    fun `findSubtree returns the node and all its descendants, shallowest first`() {
        val subtree = categoryRepository.findSubtree(bouquets.descendantPathPattern())

        assertEquals(
            listOf(bouquets.id, tulips.id, roses.id, redRoses.id),
            subtree.map { it.id },
            "expected depth ascending, then displayOrder ascending"
        )
    }

    @Test
    fun `findSubtree of an inner node excludes its ancestors and siblings`() {
        val subtree = categoryRepository.findSubtree(roses.descendantPathPattern())

        assertEquals(listOf(roses.id, redRoses.id), subtree.map { it.id })
    }

    @Test
    fun `findSubtree does not leak into sibling branches`() {
        val subtree = categoryRepository.findSubtree(archived.descendantPathPattern())

        assertEquals(listOf(archived.id), subtree.map { it.id })
    }

    @Test
    fun `findSubtree returns empty when nothing matches the path`() {
        assertTrue(categoryRepository.findSubtree("/999999/%").isEmpty())
    }

    @Test
    fun `findFullTree returns only active categories, shallowest first`() {
        val tree = categoryRepository.findFullTree()

        assertEquals(listOf(bouquets.id, tulips.id, roses.id, redRoses.id), tree.map { it.id })
        assertTrue(tree.none { it.id == archived.id }, "inactive categories must be excluded")
    }

    @Test
    fun `findAllByIdInAndStatusTrue keeps active ids and drops the rest`() {
        val found = categoryRepository.findAllByIdInAndStatusTrue(
            listOf(roses.id!!, tulips.id!!, archived.id!!, 999_999L)
        )

        assertEquals(setOf(roses.id, tulips.id), found.map { it.id }.toSet())
    }

    @Test
    fun `findAllByIdInAndStatusTrue returns empty for an unknown id`() {
        assertTrue(categoryRepository.findAllByIdInAndStatusTrue(listOf(999_999L)).isEmpty())
    }

    @Test
    fun `id returns the category with that id`() {
        val found = categoryRepository.id(roses.id!!)

        assertEquals(listOf(roses.id), found.map { it.id })
    }

    private fun saveRoot(
        name: String,
        slug: String,
        displayOrder: Int = 0,
        status: Boolean = true
    ): Category {
        val saved = categoryRepository.saveAndFlush(
            Category(
                name = name,
                slug = slug,
                path = "",
                parentId = null,
                depth = 0,
                displayOrder = displayOrder,
                description = "example of description",
                status = status
            )
        )
        return categoryRepository.saveAndFlush(saved.copy(path = Category.buildRootPath(saved.id!!)))
    }

    private fun saveChild(
        name: String,
        slug: String,
        parent: Category,
        displayOrder: Int = 0,
        status: Boolean = true
    ): Category {
        val saved = categoryRepository.saveAndFlush(
            Category(
                name = name,
                slug = slug,
                path = "",
                parentId = parent.id,
                depth = parent.depth + 1,
                displayOrder = displayOrder,
                description = "example of description",
                status = status
            )
        )
        return categoryRepository.saveAndFlush(
            saved.copy(path = Category.buildChildPath(parent.path, saved.id!!))
        )
    }
}

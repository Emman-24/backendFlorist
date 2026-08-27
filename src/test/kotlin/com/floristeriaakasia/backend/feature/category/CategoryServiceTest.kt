package com.floristeriaakasia.backend.feature.category

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*
import kotlin.test.*

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @InjectMocks
    private lateinit var categoryService: CategoryService

    @BeforeEach
    fun setUp() {
        categoryRepository = Mockito.mock(CategoryRepository::class.java)
        categoryService = CategoryService(categoryRepository)
    }

    private fun buildCategory(
        id: Long? = 1L,
        name: String = "Flores",
        slug: String = "flores",
        path: String = "/1/",
        parentId: Long? = null,
        depth: Int = 0,
        displayOrder: Int = 0,
        description: String? = null,
        status: Boolean = true
    ) = Category(
        id = id,
        name = name,
        slug = slug,
        path = path,
        parentId = parentId,
        depth = depth,
        displayOrder = displayOrder,
        description = description,
        status = status
    )

    @Test
    fun `getById should return category when found`() {
        // Arrange
        val category = buildCategory(id = 1L, name = "Flores")
        Mockito.`when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))

        // Act
        val result = categoryService.getById(1L)

        // Assert
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Flores", result.name)
    }

    @Test
    fun `getById should throw IllegalArgumentException when not found`() {
        // Arrange
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            categoryService.getById(99L)
        }
        assertEquals("Category not found", exception.message)
    }

    @Test
    fun `getRoots should return full tree from repository`() {
        // Arrange
        val roots = listOf(
            buildCategory(id = 1L, name = "Flores", path = "/1/"),
            buildCategory(id = 2L, name = "Plantas", path = "/2/")
        )
        Mockito.`when`(categoryRepository.findFullTree()).thenReturn(roots)

        // Act
        val result = categoryService.getRoots()

        // Assert
        assertEquals(2, result.size)
        assertEquals(roots, result)
        Mockito.verify(categoryRepository).findFullTree()
    }

    @Test
    fun `getFullTree should return full tree from repository`() {
        // Arrange
        val tree = listOf(
            buildCategory(id = 1L, name = "Flores", path = "/1/"),
            buildCategory(id = 2L, name = "Rosas", path = "/1/2/", parentId = 1L, depth = 1)
        )
        Mockito.`when`(categoryRepository.findFullTree()).thenReturn(tree)

        // Act
        val result = categoryService.getFullTree()

        // Assert
        assertEquals(2, result.size)
        assertEquals(tree, result)
        Mockito.verify(categoryRepository).findFullTree()
    }

    @Test
    fun `createRoot should save and update path with generated id`() {
        // Arrange
        val name = "Flores"
        val slug = "flores"
        val displayOrder = 1

        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<Category>(0)
            if (entity.id == null) {
                entity.copy(id = 10L)
            } else {
                entity
            }
        }

        // Act
        val result = categoryService.createRoot(name = name, slug = slug, displayOrder = displayOrder)

        // Assert
        assertEquals(10L, result.id)
        assertEquals("/10/", result.path)
        assertEquals(name, result.name)
        assertEquals(slug, result.slug)
        assertEquals(0, result.depth)
        assertNull(result.parentId)
        assertEquals(displayOrder, result.displayOrder)
        Mockito.verify(categoryRepository, Mockito.times(2)).save(Mockito.any())
    }

    @Test
    fun `createChild should find parent, save child and update path`() {
        // Arrange
        val parent = buildCategory(id = 5L, name = "Flores", path = "/5/", depth = 0)
        Mockito.`when`(categoryRepository.findById(5L)).thenReturn(Optional.of(parent))

        val childName = "Rosas"
        val childSlug = "rosas"
        val displayOrder = 2

        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<Category>(0)
            if (entity.id == null) {
                entity.copy(id = 15L)
            } else {
                entity
            }
        }

        // Act
        val result = categoryService.createChild(
            name = childName,
            slug = childSlug,
            parentId = 5L,
            displayOrder = displayOrder
        )

        // Assert
        assertEquals(15L, result.id)
        assertEquals("/5/15/", result.path)
        assertEquals(5L, result.parentId)
        assertEquals(1, result.depth)
        assertEquals(childName, result.name)
        assertEquals(childSlug, result.slug)
        Mockito.verify(categoryRepository, Mockito.times(2)).save(Mockito.any())
    }

    @Test
    fun `createChild should throw IllegalArgumentException when parent does not exist`() {
        // Arrange
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            categoryService.createChild(name = "Orquídeas", slug = "orquideas", parentId = 99L)
        }
        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `buildTreeFromFlat should construct tree hierarchy and sort roots by displayOrder`() {
        // Arrange
        val root1 = buildCategory(id = 1L, name = "Root 1", path = "/1/", parentId = null, depth = 0, displayOrder = 2)
        val root2 = buildCategory(id = 2L, name = "Root 2", path = "/2/", parentId = null, depth = 0, displayOrder = 1)
        val child1 = buildCategory(id = 3L, name = "Child 1-1", path = "/1/3/", parentId = 1L, depth = 1, displayOrder = 1)
        val child2 = buildCategory(id = 4L, name = "Child 1-2", path = "/1/4/", parentId = 1L, depth = 1, displayOrder = 2)
        val grandChild = buildCategory(id = 5L, name = "Grandchild 1-1-1", path = "/1/3/5/", parentId = 3L, depth = 2, displayOrder = 1)

        val categories = listOf(root1, root2, child1, child2, grandChild)

        // Act
        val tree = categoryService.buildTreeFromFlat(categories)

        // Assert
        assertEquals(2, tree.size)
        // Root 2 has displayOrder 1, Root 1 has displayOrder 2
        assertEquals(2L, tree[0].category.id)
        assertEquals(0, tree[0].children.size)

        assertEquals(1L, tree[1].category.id)
        assertEquals(2, tree[1].children.size)

        val child1Node = tree[1].children.first { it.category.id == 3L }
        assertEquals(1, child1Node.children.size)
        assertEquals(5L, child1Node.children[0].category.id)
    }

    @Test
    fun `buildTreeFromFlat should return empty list when given empty input`() {
        // Act
        val tree = categoryService.buildTreeFromFlat(emptyList())

        // Assert
        assertTrue(tree.isEmpty())
    }

    @Test
    fun `update should update existing category properties`() {
        // Arrange
        val existing = buildCategory(id = 1L, name = "Old Name", slug = "old-slug", displayOrder = 0, description = "Old")
        Mockito.`when`(categoryRepository.findById(1L)).thenReturn(Optional.of(existing))
        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) as Category }

        // Act
        val updated = categoryService.update(
            id = 1L,
            name = "New Name",
            slug = "new-slug",
            displayOrder = 5,
            description = "New Desc"
        )

        // Assert
        assertEquals(1L, updated.id)
        assertEquals("New Name", updated.name)
        assertEquals("new-slug", updated.slug)
        assertEquals(5, updated.displayOrder)
        assertEquals("New Desc", updated.description)
    }

    @Test
    fun `update should throw IllegalArgumentException when category not found`() {
        // Arrange
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            categoryService.update(id = 99L, name = "Name", slug = "slug", displayOrder = 0, description = null)
        }
        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `setStatus should update status of category`() {
        // Arrange
        val existing = buildCategory(id = 1L, status = true)
        Mockito.`when`(categoryRepository.findById(1L)).thenReturn(Optional.of(existing))
        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) as Category }

        // Act
        val result = categoryService.setStatus(id = 1L, status = false)

        // Assert
        assertFalse(result.status)
        assertEquals(1L, result.id)
    }

    @Test
    fun `setStatus should throw IllegalArgumentException when category not found`() {
        // Arrange
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            categoryService.setStatus(id = 99L, status = false)
        }
        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any())
    }

    @Test
    fun `move should move a child category to root when newParentId is null`() {
        // Arrange
        val category = buildCategory(id = 2L, name = "Rosas", path = "/1/2/", parentId = 1L, depth = 1)
        val childOf2 = buildCategory(id = 3L, name = "Rosas Rojas", path = "/1/2/3/", parentId = 2L, depth = 2)

        Mockito.`when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) as Category }
        Mockito.`when`(categoryRepository.findSubtree("/1/2/%")).thenReturn(listOf(category, childOf2))

        // Act
        val moved = categoryService.move(id = 2L, newParentId = null)

        // Assert
        assertNull(moved.parentId)
        assertEquals(0, moved.depth)
        assertEquals("/2/", moved.path)

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Category>>
        Mockito.verify(categoryRepository).saveAll(captor.capture())

        val savedDescendants = captor.value
        assertEquals(1, savedDescendants.size)
        val updatedChild = savedDescendants[0]
        assertEquals(3L, updatedChild.id)
        assertEquals("/2/3/", updatedChild.path)
        assertEquals(1, updatedChild.depth)
    }

    @Test
    fun `move should move category under new parent and update descendants path and depth`() {
        // Arrange
        val category = buildCategory(id = 2L, name = "Rosas", path = "/1/2/", parentId = 1L, depth = 1)
        val newParent = buildCategory(id = 10L, name = "Plantas", path = "/10/", parentId = null, depth = 0)
        val childOf2 = buildCategory(id = 3L, name = "Rosas Rojas", path = "/1/2/3/", parentId = 2L, depth = 2)

        Mockito.`when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        Mockito.`when`(categoryRepository.findById(10L)).thenReturn(Optional.of(newParent))
        Mockito.`when`(categoryRepository.save(Mockito.any())).thenAnswer { it.getArgument(0) as Category }
        Mockito.`when`(categoryRepository.findSubtree("/1/2/%")).thenReturn(listOf(category, childOf2))

        // Act
        val moved = categoryService.move(id = 2L, newParentId = 10L)

        // Assert
        assertEquals(10L, moved.parentId)
        assertEquals(1, moved.depth)
        assertEquals("/10/2/", moved.path)

        @Suppress("UNCHECKED_CAST")
        val captor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<Category>>
        Mockito.verify(categoryRepository).saveAll(captor.capture())

        val savedDescendants = captor.value
        assertEquals(1, savedDescendants.size)
        val updatedChild = savedDescendants[0]
        assertEquals(3L, updatedChild.id)
        assertEquals("/10/2/3/", updatedChild.path)
        assertEquals(2, updatedChild.depth)
    }

    @Test
    fun `move should throw IllegalArgumentException when category is already under target parent`() {
        // Arrange
        val category = buildCategory(id = 2L, parentId = 1L, path = "/1/2/")
        Mockito.`when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            categoryService.move(id = 2L, newParentId = 1L)
        }
        assertTrue(exception.message!!.contains("already under this parent"))
    }

    @Test
    fun `move should throw IllegalArgumentException when moving category under itself`() {
        // Arrange
        val category = buildCategory(id = 2L, parentId = 1L, path = "/1/2/")
        Mockito.`when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            categoryService.move(id = 2L, newParentId = 2L)
        }
        assertTrue(exception.message!!.contains("own descendant") || exception.message!!.contains("already"))
    }

    @Test
    fun `move should throw IllegalArgumentException when moving category under its descendant`() {
        // Arrange
        val category = buildCategory(id = 1L, parentId = null, path = "/1/", depth = 0)
        val descendant = buildCategory(id = 3L, parentId = 2L, path = "/1/2/3/", depth = 2)

        Mockito.`when`(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        Mockito.`when`(categoryRepository.findById(3L)).thenReturn(Optional.of(descendant))

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            categoryService.move(id = 1L, newParentId = 3L)
        }
        assertTrue(exception.message!!.contains("Cannot move a category under its own descendant"))
    }

    @Test
    fun `move should throw IllegalArgumentException when target category does not exist`() {
        // Arrange
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            categoryService.move(id = 99L, newParentId = 1L)
        }
    }

    @Test
    fun `move should throw IllegalArgumentException when new parent category does not exist`() {
        // Arrange
        val category = buildCategory(id = 2L, parentId = 1L, path = "/1/2/")
        Mockito.`when`(categoryRepository.findById(2L)).thenReturn(Optional.of(category))
        Mockito.`when`(categoryRepository.findById(99L)).thenReturn(Optional.empty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            categoryService.move(id = 2L, newParentId = 99L)
        }
    }
}
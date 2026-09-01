package com.floristeriaakasia.backend.feature.category

import com.floristeriaakasia.backend.feature.category.infrastructure.api.*
import com.floristeriaakasia.backend.util.ApiResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@ExtendWith(MockitoExtension::class)
class CategoryControllerTest {

    @Mock
    private lateinit var categoryService: CategoryService

    @InjectMocks
    private lateinit var categoryController: CategoryController

    @BeforeEach
    fun setUp() {
        categoryService = Mockito.mock(CategoryService::class.java)
        categoryController = CategoryController(categoryService)
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
    fun createRoot() {
        // Arrange
        val request = CreateCategoryRequest(
            name = "Flores",
            slug = "flores",
            displayOrder = 1,
            description = "Example of description"
        )
        val createdCategory = buildCategory(
            id = 1L,
            name = "Flores",
            slug = "flores",
            path = "/1/",
            parentId = null,
            depth = 0,
            displayOrder = 1,
            description = null,
            status = true
        )
        Mockito.`when`(categoryService.createRoot("Flores", "flores", 1, description = "Example of description"))
            .thenReturn(createdCategory)

        // Act
        val responseBuilder = categoryController.createRoot(request)
        val response: ResponseEntity<Void> = responseBuilder.build()

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)

        Mockito.verify(categoryService).createRoot("Flores", "flores", 1, description = "Example of description")
    }

    @Test
    fun createChild() {
        // Arrange
        val parentId = 1L
        val request = CreateCategoryRequest(
            name = "Rosas",
            slug = "rosas",
            displayOrder = 2,
            description = "Example of description"
        )
        val createdChild = buildCategory(
            id = 2L,
            name = "Rosas",
            slug = "rosas",
            path = "/1/2/",
            parentId = parentId,
            depth = 1,
            displayOrder = 2,
            description = null,
            status = true
        )
        Mockito.`when`(
            categoryService.createChild(
                "Rosas",
                "rosas",
                parentId,
                2,
                description = "Example of description"
            )
        )
            .thenReturn(createdChild)

        // Act
        val responseBuilder = categoryController.createChild(parentId, request)
        val response: ResponseEntity<ApiResponse<Void>> = responseBuilder.build()

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.body == null)

        Mockito.verify(categoryService)
            .createChild("Rosas", "rosas", parentId, 2, description = "Example of description")
    }

    @Test
    fun getFullTree() {
        // Arrange
        val flatList = listOf(
            buildCategory(id = 1L, name = "Flores", slug = "flores", path = "/1/"),
            buildCategory(id = 2L, name = "Rosas", slug = "rosas", path = "/1/2/", parentId = 1L, depth = 1)
        )
        val treeNodes = listOf(
            CategoryNode(
                category = flatList[0],
                children = mutableListOf(
                    CategoryNode(category = flatList[1])
                )
            )
        )
        Mockito.`when`(categoryService.getFullTree()).thenReturn(flatList)
        Mockito.`when`(categoryService.buildTreeFromFlat(flatList)).thenReturn(treeNodes)

        // Act
        val response = categoryController.getFullTree()

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<List<CategoryTreeResponse>>
        val data = successBody.data
        assertEquals(1, data.size)

        val rootNode = data[0]
        assertEquals(1L, rootNode.category.id)
        assertEquals("Flores", rootNode.category.name)
        assertEquals(1, rootNode.children.size)

        val childNode = rootNode.children[0]
        assertEquals(2L, childNode.category.id)
        assertEquals("Rosas", childNode.category.name)
        assertEquals(1L, childNode.category.parentId)
        assertEquals(0, childNode.children.size)

        Mockito.verify(categoryService).getFullTree()
        Mockito.verify(categoryService).buildTreeFromFlat(flatList)
    }

    @Test
    fun getRoots() {
        // Arrange
        val roots = listOf(
            buildCategory(id = 1L, name = "Flores", slug = "flores", path = "/1/"),
            buildCategory(id = 2L, name = "Plantas", slug = "plantas", path = "/2/")
        )
        Mockito.`when`(categoryService.getRoots()).thenReturn(roots)

        // Act
        val response = categoryController.getRoots()

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<List<CategoryResponse>>
        val data = successBody.data
        assertEquals(2, data.size)
        assertEquals(1L, data[0].id)
        assertEquals("Flores", data[0].name)
        assertEquals(2L, data[1].id)
        assertEquals("Plantas", data[1].name)

        Mockito.verify(categoryService).getRoots()
    }

    @Test
    fun getById() {
        // Arrange
        val category = buildCategory(id = 1L, name = "Flores", slug = "flores", path = "/1/")
        Mockito.`when`(categoryService.getById(1L)).thenReturn(category)

        // Act
        val response = categoryController.getById(1L)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<CategoryResponse>
        val data = successBody.data
        assertEquals(1L, data.id)
        assertEquals("Flores", data.name)
        assertEquals("flores", data.slug)
        assertEquals("/1/", data.path)

        Mockito.verify(categoryService).getById(1L)
    }

    @Test
    fun update() {
        // Arrange
        val request = UpdateCategoryRequest(
            name = "Flores Exoticas",
            slug = "flores-exoticas",
            displayOrder = 5,
            description = "Flores de temporada y exoticas"
        )
        val updatedCategory = buildCategory(
            id = 1L,
            name = "Flores Exoticas",
            slug = "flores-exoticas",
            path = "/1/",
            displayOrder = 5,
            description = "Flores de temporada y exoticas"
        )
        Mockito.`when`(
            categoryService.update(
                1L,
                "Flores Exoticas",
                "flores-exoticas",
                5,
                "Flores de temporada y exoticas"
            )
        )
            .thenReturn(updatedCategory)

        // Act
        val response = categoryController.update(1L, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<CategoryResponse>
        val data = successBody.data
        assertEquals(1L, data.id)
        assertEquals("Flores Exoticas", data.name)
        assertEquals("flores-exoticas", data.slug)
        assertEquals(5, data.displayOrder)
        assertEquals("Flores de temporada y exoticas", data.description)

        Mockito.verify(categoryService)
            .update(1L, "Flores Exoticas", "flores-exoticas", 5, "Flores de temporada y exoticas")
    }

    @Test
    fun move() {
        // Arrange
        val request = MoveCategoryRequest(newParentId = 3L)
        val movedCategory = buildCategory(
            id = 1L,
            name = "Flores",
            slug = "flores",
            path = "/3/1/",
            parentId = 3L,
            depth = 1
        )
        Mockito.`when`(categoryService.move(1L, 3L)).thenReturn(movedCategory)

        // Act
        val response = categoryController.move(1L, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<CategoryResponse>
        val data = successBody.data
        assertEquals(1L, data.id)
        assertEquals(3L, data.parentId)
        assertEquals(1, data.depth)
        assertEquals("/3/1/", data.path)

        Mockito.verify(categoryService).move(1L, 3L)
    }

    @Test
    fun updateStatus() {
        // Arrange
        val request = UpdateCategoryStatusRequest(status = false)
        val statusUpdatedCategory = buildCategory(
            id = 1L,
            name = "Flores",
            slug = "flores",
            status = false
        )
        Mockito.`when`(categoryService.setStatus(1L, false)).thenReturn(statusUpdatedCategory)

        // Act
        val response = categoryController.updateStatus(1L, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body is ApiResponse.Success)
        val successBody = response.body as ApiResponse.Success<CategoryResponse>
        val data = successBody.data
        assertEquals(1L, data.id)
        assertFalse(data.isActive)

        Mockito.verify(categoryService).setStatus(1L, false)
    }

}
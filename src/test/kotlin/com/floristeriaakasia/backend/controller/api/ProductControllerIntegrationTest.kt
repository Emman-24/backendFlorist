package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.config.IntegrationTestBase
import com.floristeriaakasia.backend.model.*
import com.floristeriaakasia.backend.model.dto.*
import com.floristeriaakasia.backend.repository.*
import com.floristeriaakasia.backend.security.JwtService
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import java.math.BigDecimal
import kotlin.test.assertEquals


class ProductControllerIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var subcategoryRepository: SubcategoryRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var adminToken: String
    private lateinit var category: Category
    private lateinit var subcategory: SubCategory

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        subcategoryRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()

        category = categoryRepository.save(Category(text = "Test Category", route = "test-category"))

        val subCategoryToSave = SubCategory(text = "Test Subcategory", route = "test-subcategory")
        subCategoryToSave.category = category
        subcategory = subcategoryRepository.save(subCategoryToSave)

        adminToken = createAdminAndGetToken()

    }

    private fun createAdminAndGetToken(): String {
        val adminRole = roleRepository.save(Role(name = Role.ADMIN))
        val adminUser = User(
            username = "admin",
            email = "admin@test.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(adminRole)
        )
        val savedUser = userRepository.save(adminUser)
        return jwtService.generateToken(savedUser)
    }

    @Test
    fun `should create product successfully`() {
        val request = ProductCreateRequest(
            title = "Test Product",
            slug = "test-product",
            price = BigDecimal("100.00"),
            categoryId = category.id!!,
            subcategoryId = subcategory.id!!
        )

        val response = testRestTemplate.exchange(
            "${getBaseUrl()}/api/products",
            HttpMethod.POST,
            HttpEntity(request, createHeaders(adminToken)),
            Map::class.java
        )

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)

        val data = (response.body as Map<String, Any>)["data"] as Map<String, Any>
        assertEquals("Test Product", data["title"])
        assertEquals("test-product", data["slug"])
    }

    @Test
    fun `should get product by id`() {
        val product = createTestProduct()

        val response = testRestTemplate.exchange(
            "${getBaseUrl()}/api/products/${product.id}",
            HttpMethod.GET,
            HttpEntity(null, createHeaders(adminToken)),
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `should return 404 for non-existent product`() {
        val response = testRestTemplate.getForEntity(
            "${getBaseUrl()}/api/products/99999",
            Map::class.java
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should update product`(){
        val product = createTestProduct()

        val updateRequest = ProductCreateRequest(
            title = "Updated Product",
            slug = product.slug,
            price = BigDecimal("150.00"),
            categoryId = category.id!!,
            subcategoryId = subcategory.id!!,
            stockStatus = StockStatus.AVAILABLE,
            seasonal = false,
            featured = true,
            status = true
        )

        val response = testRestTemplate.exchange(
            "${getBaseUrl()}/api/products/${product.id}",
            HttpMethod.PUT,
            HttpEntity(updateRequest, createHeaders(adminToken)),
            Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)

        val data = (response.body as Map<String, Any>)["data"] as Map<String, Any>
        assertEquals("Updated Product", data["title"])
        Assertions.assertTrue(data["featured"] as Boolean)
    }

    private fun createTestProduct(): Product {
        return productRepository.save(
            Product(
                title = "Test Product",
                slug = "test-product",
                price = BigDecimal("100.00")
            ).apply {
                this.category = this@ProductControllerIntegrationTest.category
                this.subCategory = subcategory
            }
        )
    }


    private fun getAdminToken():String {
        return "test-admin-token"
    }

    private fun createHeaders(token:String): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token)
        return headers
    }


}
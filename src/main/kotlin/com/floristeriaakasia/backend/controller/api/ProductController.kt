package com.floristeriaakasia.backend.controller.api


import com.floristeriaakasia.backend.controller.response.ApiResponse
import com.floristeriaakasia.backend.controller.response.toErrorResponse
import com.floristeriaakasia.backend.controller.response.toSuccessResponse
import com.floristeriaakasia.backend.model.dto.*
import com.floristeriaakasia.backend.service.*
import com.floristeriaakasia.backend.util.HtmlSanitizer
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
class ProductController(
    private val productService: ProductService,
    private val productTagService: ProductTagService,
    private val productImageService: ProductImageService,
    private val productDescriptionService: ProductDescriptionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    @Operation(summary = "Get all products")
    fun getAllProducts(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) subcategoryId: Long?,
        @RequestParam(required = false) featured: Boolean?,
        @RequestParam(required = false) seasonal: Boolean?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ProductListDTO>> {

        val products = productService.findAllWithFilters(
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            featured = featured,
            seasonal = seasonal,
            pageable = pageable
        )

        val dtos = products.map { ProductListDTO.from(it) }
        return ResponseEntity.ok(dtos)
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProductById(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<ProductDetailDTO>> {
        val product = productService.findById(id)
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Product not found".toErrorResponse())

        productService.incrementViews(id)

        return ResponseEntity.ok(ProductDetailDTO.from(product).toSuccessResponse())
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    fun getProductBySlug(
        @PathVariable slug: String
    ): ResponseEntity<ApiResponse<ProductDetailDTO>> {
        val product = productService.findBySlug(slug)
            ?: return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Product not found".toErrorResponse())

        productService.incrementViews(product.id!!)

        return ResponseEntity.ok(ProductDetailDTO.from(product).toSuccessResponse())

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create product")
    fun createProduct(
        @Valid @RequestBody request: ProductCreateRequest
    ): ResponseEntity<ApiResponse<ProductDetailDTO>> {
        return try {
            val sanitizedRequest = sanitizeProductRequest(request)
            val product = productService.create(sanitizedRequest)
            logger.info("Product created successfully: id={}, slug={}", product.id, product.slug)
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductDetailDTO.from(product).toSuccessResponse("Product created successfully"))
        } catch (e: IllegalArgumentException) {
            logger.warn("XSS attempt detected in product creation: title={}, error={}", request.title, e.message)
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body((e.message ?: "Invalid product data").toErrorResponse())
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update product")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductCreateRequest
    ): ResponseEntity<ApiResponse<ProductDetailDTO>> {
        return try {
            val sanitizedRequest = sanitizeProductRequest(request)
            val product = productService.update(id, sanitizedRequest)
            logger.info("Product updated successfully: id={}, slug={}", product.id, product.slug)
            ResponseEntity.ok(
                ProductDetailDTO.from(product).toSuccessResponse("Product updated successfully")
            )
        } catch (e: IllegalArgumentException) {
            logger.warn("XSS attempt detected in product update: id={}, title={}, error={}", id, request.title, e.message)
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body((e.message ?: "Invalid product data").toErrorResponse())
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete product")
    fun deleteProduct(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        productService.delete(id)
        return ResponseEntity.ok(
            Unit.toSuccessResponse("Product deleted successfully")
        )
    }

    @PutMapping("/{id}/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun assignTags(
        @PathVariable id: Long,
        @RequestBody tagIds: List<Long>
    ): ResponseEntity<ApiResponse<Unit>> {
        productTagService.assignTags(id, tagIds)
        return ResponseEntity.ok(Unit.toSuccessResponse("Product tags updated successfully"))
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun uploadImage(
        @PathVariable id: Long,
        @RequestParam file: MultipartFile,
        @RequestParam(required = false) altText: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "false") seasonal: Boolean
    ): ResponseEntity<ApiResponse<ProductGalleryDTO>> {
        val product = productService.findById(id) ?: return ResponseEntity.notFound().build()

        // Sanitize altText to prevent XSS
        val sanitizedAltText = altText?.let { HtmlSanitizer.sanitizeText(it) }

        val gallery = productImageService.uploadImage(product, file, sanitizedAltText, isPrimary, seasonal)
        return ResponseEntity.ok(ProductGalleryDTO.from(gallery).toSuccessResponse())
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteImage(
        @PathVariable productId: Long,
        @PathVariable imageId: Long
    ):ResponseEntity<ApiResponse<Unit>>{
        productImageService.deleteImage(imageId)
        return ResponseEntity.ok(Unit.toSuccessResponse("Image deleted"))
    }

    @PostMapping("/{id}/descriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun addDescription(
        @PathVariable id: Long,
        @RequestBody request: DescriptionCreateRequest
    ):ResponseEntity<ApiResponse<ProductDescriptionDTO>>{

        if (HtmlSanitizer.containsHtml(request.paragraph)) {
            logger.warn("XSS attempt detected in product description: productId={}", id)
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid characters detected in description".toErrorResponse())
        }

        // Sanitize paragraph content
        val sanitizedParagraph = HtmlSanitizer.sanitizeRichText(request.paragraph)
            ?: throw IllegalArgumentException("Invalid description content")

        val description = productDescriptionService.addDescription(
            id,
            sanitizedParagraph,
            request.position
        )
        logger.info("Description added to product: productId={}, descriptionId={}", id, description.id)
        return ResponseEntity.ok(ProductDescriptionDTO.from(description).toSuccessResponse())
    }

    @DeleteMapping("/{productId}/descriptions/{descId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun deleteDescription(
        @PathVariable productId: Long,
        @PathVariable descId: Long
    ):ResponseEntity<ApiResponse<Unit>>{
        productDescriptionService.deleteDescription(descId)
        return ResponseEntity.ok(Unit.toSuccessResponse())
    }



    private fun sanitizeProductRequest(request: ProductCreateRequest): ProductCreateRequest {
        // Detect XSS attempts
        if (HtmlSanitizer.containsHtml(request.title) ||
            HtmlSanitizer.containsHtml(request.slug) ||
            HtmlSanitizer.containsHtml(request.facebookUrl) ||
            HtmlSanitizer.containsHtml(request.instagramUrl)) {
            throw IllegalArgumentException("Invalid characters detected in product data")
        }

        // Sanitize inputs
        val sanitizedTitle = HtmlSanitizer.sanitizeProductTitle(request.title)
            ?: throw IllegalArgumentException("Invalid product title")
        val sanitizedSlug = HtmlSanitizer.sanitizeSlug(request.slug)
            ?: throw IllegalArgumentException("Invalid product slug")
        val sanitizedFacebookUrl = request.facebookUrl?.let { HtmlSanitizer.sanitizeUrl(it) }
        val sanitizedInstagramUrl = request.instagramUrl?.let { HtmlSanitizer.sanitizeUrl(it) }


        return request.copy(
            title = sanitizedTitle,
            slug = sanitizedSlug,
            facebookUrl = sanitizedFacebookUrl,
            instagramUrl = sanitizedInstagramUrl
        )
    }

}
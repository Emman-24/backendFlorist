package com.floristeriaakasia.backend.feature.product.adapter.`in`.web

import com.floristeriaakasia.backend.feature.product.adapter.`in`.web.dto.CreateProductRequest
import com.floristeriaakasia.backend.feature.product.application.port.`in`.CreateProductCommand
import com.floristeriaakasia.backend.feature.product.application.service.AddImageToProductService
import com.floristeriaakasia.backend.feature.product.application.service.CreateProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val addImageToProductService: AddImageToProductService,
    private val createProductService: CreateProductService
) {
    @PostMapping("/{productId}/images")
    fun uploadProductImage(
        @PathVariable productId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("altText") altText: String
    ): ResponseEntity<Void> {
        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }
        addImageToProductService.execute(productId, file, altText)
        return ResponseEntity.noContent().build()
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest
    ): ResponseEntity.BodyBuilder {
        val command = CreateProductCommand(
            name = request.name,
            categoryId = request.categoryId,
            priceAmount = request.priceAmount,
            currency = request.currency,
            seasonal = request.seasonal,
            featured = request.featured,
            slug = request.slug,
            facebookUrl = request.facebookUrl,
            instagramUrl = request.instagramUrl
        )
        createProductService.execute(command)
        return ResponseEntity.status(HttpStatus.CREATED)
    }

}

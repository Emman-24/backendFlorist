package com.floristeriaakasia.backend.controller.api

import com.floristeriaakasia.backend.model.dto.product.ProductResponse
import com.floristeriaakasia.backend.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getProducts(
        @RequestParam(required = false, defaultValue = "true") status: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int
    ): Page<ProductResponse> {
        return productService.findByStatus(status, page, size)
    }


}
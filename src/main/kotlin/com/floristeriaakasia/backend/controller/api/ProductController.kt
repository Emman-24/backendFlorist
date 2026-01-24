package com.floristeriaakasia.backend.controller.api


import com.floristeriaakasia.backend.service.ProductSeoService
import com.floristeriaakasia.backend.service.ProductService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val productSeoService: ProductSeoService
) {



}
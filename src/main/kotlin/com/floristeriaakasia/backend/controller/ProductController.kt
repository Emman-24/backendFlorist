package com.floristeriaakasia.backend.controller

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.repository.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/products")
class ProductController(private val productRepository: ProductRepository) {

    @GetMapping
    fun getAllProducts() = productRepository.findAll()

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        val product = productRepository.findById(id)
        return if (product.isPresent) ResponseEntity(product.get(), HttpStatus.OK)
        else ResponseEntity(HttpStatus.NOT_FOUND)
    }

}
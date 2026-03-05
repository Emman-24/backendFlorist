package com.floristeriaakasia.backend.feature.product.application.port.`in`

import org.springframework.web.multipart.MultipartFile

interface AddImageToProductUseCase {
    fun execute(productId: Long, file: MultipartFile, altText: String)
}
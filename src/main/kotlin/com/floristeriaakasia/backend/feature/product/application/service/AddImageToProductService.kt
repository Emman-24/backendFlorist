package com.floristeriaakasia.backend.feature.product.application.service

import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductPersistenceAdapter
import com.floristeriaakasia.backend.feature.product.application.port.`in`.AddImageToProductUseCase
import com.floristeriaakasia.backend.feature.product.application.port.out.ImageStoragePort
import com.floristeriaakasia.backend.feature.product.domain.exception.ProductNotFoundException
import com.floristeriaakasia.backend.feature.product.domain.model.ProductGalleryDomain
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class AddImageToProductService(
    private val productRepository: ProductPersistenceAdapter,
    private val imageStorage: ImageStoragePort
): AddImageToProductUseCase {

    @Transactional
    override fun execute(
        productId: Long,
        file: MultipartFile,
        altText: String
    ) {
        val product = productRepository.findById(productId) ?: throw ProductNotFoundException(productId)
        val uploadedImage = imageStorage.uploadImage(file, "products/${product.slug}")
        val newGalleryImage = ProductGalleryDomain(
            publicId = uploadedImage.publicId,
            originalUrl = uploadedImage.original,
            thumbnailUrl = uploadedImage.thumbnail,
            mediumUrl = uploadedImage.medium,
            altText = altText,
            position = product.gallery.size
        )
        product.addImage(newGalleryImage)
        productRepository.save(product)
    }

}
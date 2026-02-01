package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductGallery
import com.floristeriaakasia.backend.repository.ProductGalleryRepository
import org.hibernate.internal.HEMLogging.logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ProductImageService(
    private val galleryRepository: ProductGalleryRepository,
    private val imageStorageService: ImageStorageService
) {

    @Transactional
    fun uploadImage(
        product: Product,
        file: MultipartFile,
        altText: String?,
        isPrimary: Boolean,
        seasonal: Boolean
    ): ProductGallery {
        validateImage(file)

        if (isPrimary) {
            product.gallery.forEach { it.isPrimary = false }
        }

        val imageUrls = imageStorageService.upload(
            file,
            "products/${product.category.route}/${product.subCategory.route}"
        )

        val gallery = ProductGallery(
            originalName = file.originalFilename ?: "image.jpg",
            storedName = imageUrls.publicId,
            mimeType = file.contentType ?: "image/jpeg",
            size = file.size,
            altText = altText ?: "${product.title} - Floristería Akasia - Pereira - Colombia",
            isPrimary = isPrimary,
            position = product.gallery.size,
            seasonal = seasonal,
            status = true
        ).apply {
            this.product = product
        }

        return galleryRepository.save(gallery)

    }

    @Transactional
    fun deleteImage(imageId: Long) {
        val image = galleryRepository.findByIdOrNull(imageId)
            ?: throw ResourceNotFoundException("Image with id $imageId not found")

        try {
            imageStorageService.delete(image.storedName)
        }catch (e: Exception) {
            println("Warning: Failed to delete image file ${image.storedName}: ${e.message}")
        }
        galleryRepository.delete(image)
    }



    private fun validateImage(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty.")
        }

        val allowedTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("Invalid image type: ${file.contentType}")
        }

        val maxSize = 10 * 1024 * 1204
        if (file.size > maxSize) {
            throw IllegalArgumentException("File size must be less than $maxSize bytes.")
        }
    }
}
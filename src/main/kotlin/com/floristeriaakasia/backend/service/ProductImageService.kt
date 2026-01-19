package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductGallery
import com.floristeriaakasia.backend.repository.ProductGalleryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class ProductImageService(
    private val productGalleryRepository: ProductGalleryRepository,
    private val imageStorageService: LocalImageStorageService,
    private val properties: ImageStorageProperties
) {


    @Transactional
    fun uploadImage(
        product: Product,
        file: MultipartFile,
        altText: String,
        isPrimary: Boolean,
        seasonal: Boolean
    ): ProductGallery {
        validateImage(file)

        if (isPrimary) {
            product.gallery.forEach { it.isPrimary = false }
        }

        val storedPath = try {
            imageStorageService.storeFile(
                inputStream = file.inputStream,
                originalFileName = file.originalFilename ?: "image.jpg",
                categoryName = product.category.text,
                subcategoryName = product.subCategory.text
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to store image file: ${e.message}", e)
        }

        val gallery = ProductGallery(
            originalName = file.originalFilename ?: "image.jpg",
            storedName = storedPath,
            mimeType = file.contentType ?: "image/jpeg",
            size = file.size,
            altText = altText ?: "${product.title} - Floristeria Akasia - Pereira - Colombia",
            isPrimary = isPrimary,
            position = product.gallery.size,
            seasonal = seasonal,
            status = true
        ).apply {
            this.product = product
        }

        return try {
            productGalleryRepository.save(gallery)
        } catch (e: Exception) {
            imageStorageService.deleteFile(storedPath)
            throw e
        }

    }

    @Transactional
    fun deleteImage(imageId: Long) {
        val image = productGalleryRepository.findByIdOrNull(imageId)
            ?: throw ResourceNotFoundException("Image with id $imageId not found")
        val storedPath = image.storedName
        productGalleryRepository.delete(image)

        try {
            imageStorageService.deleteFile(storedPath)
        } catch (e: Exception) {
            println("Warning: Failed to delete image file $storedPath: ${e.message}")
        }
    }

    @Transactional
    fun deleteAllImages(product: Product) {
        val images = product.gallery.toList()

        images.forEach { image ->
            try {
                imageStorageService.deleteFile(image.storedName)
            } catch (e: Exception) {
                println("Warning: Failed to delete image file ${image.storedName}: ${e.message}")
            }
        }
        productGalleryRepository.deleteAll(images)
    }

    @Transactional
    fun setPrimaryImage(imageId: Long) {
        val image = productGalleryRepository.findByIdOrNull(imageId)
            ?: throw ResourceNotFoundException("Image with id $imageId not found")
        image.product.gallery.forEach { it.isPrimary = false }
        image.isPrimary = true
        productGalleryRepository.save(image)

    }

    @Transactional(readOnly = true)
    fun getProductImages(product: Product): List<ProductGallery> {
        return productGalleryRepository.findByProductOrderByPositionAsc(product)
    }

    private fun validateImage(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty.")
        }
        val mimeType = file.contentType

        if (mimeType == null || !properties.allowedMimeTypes.contains(mimeType)) {
            throw IllegalArgumentException("Invalid mime type")
        }

        val maxSize = 5 * 1024 * 1204
        if (file.size > maxSize) {
            throw IllegalArgumentException("File size must be less than $maxSize bytes.")
        }
    }
}
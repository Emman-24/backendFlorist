package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductGallery
import com.floristeriaakasia.backend.repository.ProductGalleryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import javax.imageio.IIOException

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
            throw IllegalArgumentException("El archivo está vacío")
        }

        val maxSize = 5 * 1024 * 1024L // 5MB
        if (file.size > maxSize) {
            throw IllegalArgumentException(
                "El archivo excede el tamaño máximo de 5MB. " +
                        "Tamaño actual: ${file.size / 1024 / 1024}MB"
            )
        }

        val allowedTypes = setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
        )

        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException(
                "Tipo de archivo no permitido: ${file.contentType}. " +
                        "Permitidos: JPEG, PNG, WEBP"
            )
        }

        try {
            val image = javax.imageio.ImageIO.read(file.inputStream) ?: throw IllegalArgumentException(
                "El archivo no es una imagen válida o está corrupto"
            )

            if (image.width > 5000 || image.height > 5000) {
                throw IllegalArgumentException(
                    "Las dimensiones de la imagen son demasiado grandes. " +
                            "Máximo: 5000x5000px. Actual: ${image.width}x${image.height}px"
                )
            }

            if (image.width < 200 || image.height < 200) {
                throw IllegalArgumentException(
                    "Las dimensiones de la imagen son demasiado pequeñas. " +
                            "Mínimo: 200x200px. Actual: ${image.width}x${image.height}px"
                )
            }

        } catch (e: IIOException) {
            throw IllegalArgumentException(
                "Error al procesar la imagen: ${e.message}"
            )
        } catch (e: Exception) {
            if (e is IllegalArgumentException) throw e
            throw IllegalArgumentException(
                "Error validando la imagen: ${e.message}"
            )
        }

        val filename = file.originalFilename ?: "unknown"
        if (filename.contains("..")) {
            throw IllegalArgumentException(
                "Nombre de archivo inválido: contiene path traversal"
            )
        }
    }
}
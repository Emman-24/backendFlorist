package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductGallery
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class AddImageToFloralArrangementUseCaseImpl(
    private val imageStorage: ImageStoragePort,
    private val saveFloralArrangementPort: SaveFloralArrangementPort,
) : AddImageToFloralArrangementUseCase {
    override fun execute(
        arrangementId: Long,
        image: MultipartFile,
        altText: String
    ) {
        val logger = org.slf4j.LoggerFactory.getLogger(javaClass)
        val floral = saveFloralArrangementPort.findById(arrangementId) ?: throw FloralArrangementNotFoundException(arrangementId)

        logger.info("floral arrangement id: ${floral.id}")
        val uploadedImage = imageStorage.uploadImage(image, "floralArrangements/${floral.slug}")
        val newGalleryImage = ProductGallery(
            publicId = uploadedImage.publicId,
            originalUrl = uploadedImage.original,
            floralArrangement = floral,
            thumbnailUrl = uploadedImage.thumbnail,
            mediumUrl = uploadedImage.medium,
            altText = altText,
            position = floral.gallery.size
        )
        floral.addImage(newGalleryImage)
        saveFloralArrangementPort.save(floral)
    }
}


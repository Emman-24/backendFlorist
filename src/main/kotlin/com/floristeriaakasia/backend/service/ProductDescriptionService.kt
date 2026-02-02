package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.ProductDescription
import com.floristeriaakasia.backend.repository.ProductDescriptionRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductDescriptionService(
    private val productDescriptionRepository: ProductDescriptionRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun addDescription(
        productId: Long,
        paragraph: String,
        position: Int? = null
    ): ProductDescription {
        val product =
            productRepository.findByIdOrNull(productId) ?: throw ResourceNotFoundException("Product not found")
        val finalPosition = position ?: product.descriptions.size

        val description = ProductDescription(
            paragraph = paragraph,
            position = finalPosition
        ).apply {
            this.product = product
        }
        return productDescriptionRepository.save(description)
    }

    @Transactional
    fun updateDescription(
        descriptionId: Long,
        paragraph: String,
        position: Int? = null
    ): ProductDescription {
        val description = productDescriptionRepository.findByIdOrNull(descriptionId) ?: throw ResourceNotFoundException(
            "Description not found"
        )
        description.paragraph = paragraph
        position?.let { description.position = it }
        return productDescriptionRepository.save(description)
    }

    @Transactional
    fun deleteDescription(
        descriptionId: Long
    ) {
        val description = productDescriptionRepository.findByIdOrNull(descriptionId) ?: throw ResourceNotFoundException(
            "Description not found"
        )
        productDescriptionRepository.delete(description)
    }

    @Transactional
    fun reorderDescriptions(
        productId: Long,
        descriptionIds: List<Long>
    ) {
        val product = productRepository.findByIdOrNull(productId) ?: throw ResourceNotFoundException("Product not found")

        descriptionIds.forEachIndexed { index, descId ->
            product.descriptions.find { it.id == descId }?.position = index
        }

        productRepository.save(product)

    }
}
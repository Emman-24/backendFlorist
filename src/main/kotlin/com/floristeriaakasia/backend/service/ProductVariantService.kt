package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.ProductVariant
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.ProductVariantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ProductVariantService(
    private val productVariantRepository: ProductVariantRepository,
    private val productRepository: ProductRepository
) {

    @Transactional
    fun addVariant(
        productId: Long,
        request: VariantCreateRequest
    ): ProductVariant {
        val product = productRepository.findByIdOrNull(productId) ?: throw ResourceNotFoundException("Product not found")

        val variant = ProductVariant(
            variantType = request.variantType,
            name = request.name,
            priceAdjustment = request.priceAdjustment,
            description = request.description ?: "",
            position = product.variants.size
        ).apply {
            this.product = product
        }

        return productVariantRepository.save(variant)
    }

    @Transactional
    fun updateVariant(
        variantId: Long,
        request: VariantUpdateRequest
    ): ProductVariant {
        val variant = productVariantRepository.findByIdOrNull(variantId) ?: throw ResourceNotFoundException("Variant not found")

        variant.apply {
            name = request.name
            priceAdjustment = request.priceAdjustment
            description = request.description ?: ""
            available = request.available
            status = request.status
        }
        return productVariantRepository.save(variant)
    }

    @Transactional
    fun deleteVariant(variantId: Long) {
        val variant = productVariantRepository.findByIdOrNull(variantId)
            ?: throw ResourceNotFoundException("Variant not found")
        productVariantRepository.delete(variant)
    }

}

data class VariantCreateRequest(
    val variantType: String,
    val name: String,
    val priceAdjustment: BigDecimal,
    val description: String?
)

data class VariantUpdateRequest(
    val name: String,
    val priceAdjustment: BigDecimal,
    val description: String?,
    val available: Boolean,
    val status: Boolean
)
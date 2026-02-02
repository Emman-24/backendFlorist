package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.Tag
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.TagRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductTagService(
    private val productRepository: ProductRepository,
    private val tagRepository: TagRepository,
) {

    @Transactional
    fun assignTags(
        productId: Long,
        tagIds: List<Long>
    ): Product {
        val product = productRepository.findByIdOrNull(productId) ?: throw ResourceNotFoundException("Product not found")
        val tags = tagRepository.findAllById(tagIds).toSet()
        product.tags.clear()
        product.tags.addAll(tags)
        return productRepository.save(product)
    }

    @Transactional
    fun removeTags(
        productId: Long,
        tagIds: List<Long>
    ): Product {
        val product = productRepository.findByIdOrNull(productId) ?: throw ResourceNotFoundException("Product not found")
        val tagsRemove = tagRepository.findAllById(tagIds).toSet()
        product.tags.removeAll(tagsRemove)
        return productRepository.save(product)
    }

    @Transactional(readOnly = true)
    fun getProductTags(productId:Long): List<Tag> {
        return productRepository.findByIdOrNull(productId)?.tags?.toList()
            ?: emptyList()
    }

}

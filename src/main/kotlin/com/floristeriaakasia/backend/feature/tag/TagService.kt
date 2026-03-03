package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.FloralArrangement
import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val productRepository: ProductRepository,
    private val tagRepository: TagRepository,
) {

    @Transactional
    fun createTag(request: CreateTagRequest): Tag {
        tagRepository.findByRoute(request.route)?.let {
            throw IllegalArgumentException("Tag with route '${request.route}' already exists")
        }
        val tag = Tag(
            text = request.text.trim(),
            route = request.route.trim().lowercase(),
            description = "",
            status = request.status
        )
        return tagRepository.save(tag)
    }

    @Transactional(readOnly = true)
    fun getAllTags(): List<Tag> {
        return tagRepository.findAll()
    }

    @Transactional
    fun assignTags(
        productId: Long,
        tagIds: List<Long>
    ): FloralArrangement {
        val product = productRepository.findByIdOrNull(productId)
        val tags = tagRepository.findAllById(tagIds).toSet()
        product!!.tags.clear()
        product.tags.addAll(tags)
        return productRepository.save(product)
    }

    @Transactional
    fun removeTags(
        productId: Long,
        tagIds: List<Long>
    ): FloralArrangement {
        val product = productRepository.findByIdOrNull(productId)
        val tagsRemove = tagRepository.findAllById(tagIds).toSet()
        product!!.tags.removeAll(tagsRemove)
        return productRepository.save(product)
    }

    @Transactional(readOnly = true)
    fun getProductTags(productId:Long): List<Tag> {
        return productRepository.findByIdOrNull(productId)?.tags?.toList()
            ?: emptyList()
    }

}
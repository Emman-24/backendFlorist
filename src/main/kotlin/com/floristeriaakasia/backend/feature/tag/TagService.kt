package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val floralArrangmentRepository: FloralArrangementRepository,
    private val tagRepository: TagRepository,
) {

    @Transactional
    fun createTag(request: CreateTagRequest): Tag {
        val normalizedRoute = request.route.trim().lowercase()
        val normalizedText = request.text.trim()
        tagRepository.findByRoute(normalizedRoute)?.let {
            throw IllegalArgumentException("Tag with route '${normalizedRoute}' already exists")
        }
        val tag = Tag(
            text = normalizedText,
            route = normalizedRoute,
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
    ): Boolean {
        val product = floralArrangmentRepository.findByIdOrNull(productId) ?: throw FloralArrangementNotFoundException(productId)
        val tags = tagRepository.findAllById(tagIds).toSet()
        product.tags.clear()
        product.tags.addAll(tags)
        floralArrangmentRepository.save(product)
        return true
    }

    @Transactional(readOnly = true)
    fun getProductTags(productId: Long): List<Tag> {
        return floralArrangmentRepository.findByIdOrNull(productId)?.tags?.toList()
            ?: emptyList()
    }

}
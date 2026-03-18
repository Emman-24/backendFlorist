package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
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

//    @Transactional
//    fun assignTags(
//        productId: Long,
//        tagIds: List<Long>
//    ): FloralArrangement {
//        val product = floralArrangmentRepository.findByIdOrNull(productId)
//        val tags = tagRepository.findAllById(tagIds).toSet()
//        product!!.tags.clear()
//        product.tags.addAll(tags)
//        return floralArrangmentRepository.save(product)
//    }
//
//    @Transactional
//    fun removeTags(
//        productId: Long,
//        tagIds: List<Long>
//    ): FloralArrangement {
//        val product = floralArrangmentRepository.findByIdOrNull(productId)
//        val tagsRemove = tagRepository.findAllById(tagIds).toSet()
//        product!!.tags.removeAll(tagsRemove)
//        return floralArrangmentRepository.save(product)
//    }

    @Transactional(readOnly = true)
    fun getProductTags(productId:Long): List<Tag> {
        return floralArrangmentRepository.findByIdOrNull(productId)?.tags?.toList()
            ?: emptyList()
    }

}
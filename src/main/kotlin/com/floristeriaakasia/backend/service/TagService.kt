package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Tag
import com.floristeriaakasia.backend.repository.TagRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    @Transactional(readOnly = true)
    fun findAll(): List<Tag> {
        return tagRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun findAllActive(): List<Tag> {
        return tagRepository.findByStatus(true)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Tag? {
        return tagRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun findByRoute(route: String): Tag? {
        return tagRepository.findByRoute(route)
    }

    @Transactional(readOnly = true)
    fun search(query: String): List<Tag> {
        return tagRepository.findByTextContainingIgnoreCaseAndStatus(query, true)
    }

    @Transactional
    fun save(tag: Tag): Tag {
        return tagRepository.save(tag)
    }

    @Transactional
    fun update(id: Long, tag: Tag): Tag {
        val existing = tagRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Tag with id $id not found")

        existing.text = tag.text
        existing.route = tag.route
        existing.status = tag.status

        return tagRepository.save(existing)
    }

    @Transactional
    fun deleteById(id: Long) {
        val tag = tagRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Tag with id $id not found")
        tagRepository.delete(tag)
    }

    @Transactional
    fun toggleStatus(id: Long): Tag {
        val tag = tagRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Tag with id $id not found")

        tag.status = tag.status
        return tagRepository.save(tag)
    }

    @Transactional(readOnly = true)
    fun getMostUsedTags(limit: Int = 10): List<TagStats> {
        return tagRepository.findAll()
            .map { tag ->
                TagStats(
                    id = tag.id!!,
                    name = tag.text,
                    route = tag.route,
                    productCount = tag.products.size,
                    status = tag.status
                )
            }
            .sortedByDescending { it.productCount }
            .take(limit)
    }

    @Transactional(readOnly = true)
    fun getStats(id: Long): TagStats? {
        val tag = tagRepository.findByIdOrNull(id) ?: return null

        return TagStats(
            id = tag.id!!,
            name = tag.text,
            route = tag.route,
            productCount = tag.products.size,
            status = tag.status
        )
    }
}

data class TagStats(
    val id: Long,
    val name: String,
    val route: String,
    val productCount: Int,
    val status: Boolean
)
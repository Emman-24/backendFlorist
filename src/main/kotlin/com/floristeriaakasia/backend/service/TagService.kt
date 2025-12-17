package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Tag
import com.floristeriaakasia.backend.model.dto.tag.TagMapper
import com.floristeriaakasia.backend.model.dto.tag.TagRequest
import com.floristeriaakasia.backend.model.dto.tag.TagResponse
import com.floristeriaakasia.backend.repository.TagRepository

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagService(
    private val repository: TagRepository,
    private val mapper: TagMapper
) {
    @Transactional(readOnly = true)
    fun findAll(): List<TagResponse> {
        return repository.findAll().map(mapper::toResponse)
    }

    @Transactional
    fun findActiveStatus(): List<TagResponse> {
        return repository.findByStatus(status = true).map(mapper::toResponse)
    }

    @Transactional
    fun create(request: TagRequest): TagResponse {
        val entity: Tag = mapper.toEntity(request)
        val saved = repository.save(entity)
        return mapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun findRequestById(id: Long): TagRequest {
        return repository.findByIdOrNull(id)
            ?.let { TagRequest(text = it.text, route = it.route, status = it.status) }
            ?: throw ResourceNotFoundException("Tag with id $id not found")
    }

    @Transactional
    fun update(id: Long, request: TagRequest): TagResponse {
        val existing = repository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Tag with id $id not found")
        mapper.updateEntityFromRequest(request, existing)
        val updated = repository.save(existing)
        return mapper.toResponse(updated)
    }

    fun deleteById(id: Long) {
        repository.deleteById(id)
    }

}
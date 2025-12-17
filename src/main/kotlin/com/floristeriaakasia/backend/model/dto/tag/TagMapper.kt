package com.floristeriaakasia.backend.model.dto.tag

import com.floristeriaakasia.backend.model.Tag
import com.floristeriaakasia.backend.model.dto.product.ProductMapper
import org.springframework.stereotype.Component

@Component
class TagMapper(
    val productMapper: ProductMapper
) {
    fun toResponse(tag: Tag): TagResponse {
        return TagResponse(
            id = tag.id!!,
            text = tag.text,
            route = tag.route,
            status = tag.status,
            products = tag.products.map(productMapper::toResponse),
            createdAt = tag.createdAt
        )
    }

    fun toEntity(request: TagRequest): Tag {
        return Tag(
            text = request.text,
            route = request.route,
            status = request.status
        )
    }

    fun updateEntityFromRequest(request: TagRequest, tag: Tag) {
        tag.text = request.text
        tag.route = request.route
        tag.status = request.status
    }


}
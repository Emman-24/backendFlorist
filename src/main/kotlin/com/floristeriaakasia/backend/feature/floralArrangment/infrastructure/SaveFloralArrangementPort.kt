package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

interface SaveFloralArrangementPort {
    fun save(arrangement: FloralArrangement): Long
    fun existsBySlug(slug: String): Boolean
    fun findById(id: Long): FloralArrangement?
    fun incrementViews(arrangementId: Long)
    fun findDetails(arrangementId: Long): FloralArrangement?
    fun findAllWithFilters(
        categoryId: Long?,
        tagId: Long?,
        featured: Boolean?,
        seasonal: Boolean?,
        isAvailable: Boolean?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<FloralArrangement>

    fun findWithCategoriesByIds(ids: List<Long>): List<FloralArrangement>
    fun findPrimaryImagesByArrangementIds(ids: Collection<Long>): List<ProductGallery>
    fun findWithTagsByIds(ids: List<Long>): List<FloralArrangement>
}
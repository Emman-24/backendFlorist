package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FloralArrangementPersistenceAdapter(
    private val floralArrangementRepo: FloralArrangementRepository
) : SaveFloralArrangementPort {

    override fun save(arrangement: FloralArrangement): Long =
        floralArrangementRepo.save(arrangement).id

    override fun existsBySlug(slug: String): Boolean =
        floralArrangementRepo.existsBySlug(slug)

    override fun findById(id: Long): FloralArrangement? {
        return floralArrangementRepo.findById(id).orElse(null)
    }

    override fun incrementViews(arrangementId: Long) {
        floralArrangementRepo.incrementViews(arrangementId)
    }

    override fun findDetails(arrangementId: Long): FloralArrangement? {
        return floralArrangementRepo.findByIdWithDetails(arrangementId)
    }

    override fun findAllWithFilters(
        categoryId: Long?,
        tagId: Long?,
        featured: Boolean?,
        seasonal: Boolean?,
        isAvailable: Boolean?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<FloralArrangement> {
        return floralArrangementRepo.findAllWithFilters(
            categoryId = categoryId,
            tagId = tagId,
            featured = featured,
            seasonal = seasonal,
            isAvailable = isAvailable,
            minPrice = minPrice,
            maxPrice = maxPrice,
            pageable = pageable
        )
    }

    override fun findBySlug(slug: String): FloralArrangement? {
        return floralArrangementRepo.findBySlug(slug)
    }

    override fun findWithCategoriesByIds(ids: List<Long>): List<FloralArrangement> {
        return floralArrangementRepo.findWithCategoriesByIds(ids)
    }

    override fun findPrimaryImagesByArrangementIds(ids: Collection<Long>): List<ProductGallery> {
        return floralArrangementRepo.findPrimaryImagesByArrangementIds(ids)
    }

    override fun findWithTagsByIds(ids: List<Long>): List<FloralArrangement> {
        return floralArrangementRepo.findWithTagsByIds(ids)
    }


}



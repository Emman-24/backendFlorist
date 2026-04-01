package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.domain.ProductGallery
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.CategorySummaryDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.DescriptionDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementDetailDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementQuery
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FloralArrangementSummaryDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.FlowerDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.ImageDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.PriceSummaryDto
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.TagSummaryDto
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import com.floristeriaakasia.backend.global.exeption.FloralArrangementSeoNameNotFoundException
import com.floristeriaakasia.backend.global.exeption.FloralArrangementSlugNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
@Transactional
class GetFloralArrangementsUseCaseImpl(
    private val saveFloralArrangementPort: SaveFloralArrangementPort,
) : GetFloralArrangementsUseCase {

    override fun execute(
        query: FloralArrangementQuery,
        pageable: Pageable
    ): Page<FloralArrangementSummaryDto> {

        val page: Page<FloralArrangement> = saveFloralArrangementPort.findAllWithFilters(
            categoryId = query.categoryId,
            tagId = query.tagId,
            featured = query.featured,
            seasonal = query.seasonal,
            isAvailable = query.isAvailable,
            minPrice = query.minPrice,
            maxPrice = query.maxPrice,
            pageable = pageable
        )

        if (page.isEmpty) return Page.empty(pageable)

        val ids: List<Long> = page.content.map { it.id }

        val categoriesMap: Map<Long, List<CategorySummaryDto>> =
            saveFloralArrangementPort.findWithCategoriesByIds(ids)
                .associate { fa ->
                    fa.id to fa.categories.map { c ->
                        CategorySummaryDto(id = c.id!!, name = c.name, slug = c.slug, path = c.path)
                    }
                }

        val tagsMap: Map<Long, List<TagSummaryDto>> =
            saveFloralArrangementPort.findWithTagsByIds(ids)
                .associate { fa ->
                    fa.id to fa.tags.map { t ->
                        TagSummaryDto(id = t.id!!, text = t.text, route = t.route)
                    }
                }

        val primaryImageMap: Map<Long, ImageDto> =
            saveFloralArrangementPort.findPrimaryImagesByArrangementIds(ids)
                .associate { g -> g.floralArrangement!!.id to g.toImageDto() }

        val dtos: List<FloralArrangementSummaryDto> = page.content.map { fa ->
            FloralArrangementSummaryDto(
                id           = fa.id,
                name         = fa.name,
                slug         = fa.slug,
                seoName      = fa.seoName,
                price        = fa.price.toPriceSummaryDto(),
                isAvailable  = fa.isAvailable,
                seasonal     = fa.seasonal,
                featured     = fa.featured,
                primaryImage = primaryImageMap[fa.id],
                categories   = categoriesMap[fa.id] ?: emptyList(),
                tags         = tagsMap[fa.id]        ?: emptyList()
            )
        }

        return PageImpl(dtos, pageable, page.totalElements)


    }

    override fun executeById(id: Long): FloralArrangementDetailDto {
        val fa = saveFloralArrangementPort.findDetails(id) ?: throw FloralArrangementNotFoundException(id)
        saveFloralArrangementPort.incrementViews(id)

        return FloralArrangementDetailDto(
            id = fa.id,
            name = fa.name,
            slug = fa.slug,
            seoName = fa.seoName,
            price = fa.price.toPriceSummaryDto(),
            isAvailable = fa.isAvailable,
            seasonal = fa.seasonal,
            featured = fa.featured,
            views = fa.views,
            description = fa.description?.let {
                DescriptionDto(
                    shortDescription = it.shortDescription,
                    description = it.description
                )
            },
            gallery = fa.gallery.sortedBy {
                it.position
            }
                .map { it.toImageDto() },
            flowers = fa.flowers.map { f ->
                FlowerDto(
                    id = f.id,
                    name = f.name,
                    meaning = f.meaning
                )
            },
            categories = fa.categories.map { c ->
                CategorySummaryDto(
                    id = c.id!!, name = c.name, slug = c.slug, path = c.path
                )
            },
            tags = fa.tags.map { t ->
                TagSummaryDto(
                    id = t.id!!,
                    text = t.text,
                    route = t.route
                )
            },
            createdAt = fa.createdAt,
            updatedAt = fa.updatedAt
        )
    }

    override fun executeBySlug(slug: String): FloralArrangementDetailDto {
        val fa = saveFloralArrangementPort.findBySlug(slug)?: throw FloralArrangementSlugNotFoundException(slug)
        saveFloralArrangementPort.incrementViews(fa.id)
        return FloralArrangementDetailDto(
            id = fa.id,
            name = fa.name,
            slug = fa.slug,
            seoName = fa.seoName,
            price = fa.price.toPriceSummaryDto(),
            isAvailable = fa.isAvailable,
            seasonal = fa.seasonal,
            featured = fa.featured,
            views = fa.views,
            description = fa.description?.let {
                DescriptionDto(
                    shortDescription = it.shortDescription,
                    description = it.description
                )
            },
            gallery = fa.gallery.sortedBy {
                it.position
            }
                .map { it.toImageDto() },
            flowers = fa.flowers.map { f ->
                FlowerDto(
                    id = f.id,
                    name = f.name,
                    meaning = f.meaning
                )
            },
            categories = fa.categories.map { c ->
                CategorySummaryDto(
                    id = c.id!!, name = c.name, slug = c.slug, path = c.path
                )
            },
            tags = fa.tags.map { t ->
                TagSummaryDto(
                    id = t.id!!,
                    text = t.text,
                    route = t.route
                )
            },
            createdAt = fa.createdAt,
            updatedAt = fa.updatedAt
        )
    }

    override fun executeBySeoName(seoName: String): FloralArrangementDetailDto {
        val fa = saveFloralArrangementPort.findBySeoName(seoName)?: throw FloralArrangementSeoNameNotFoundException(
            seoName
        )
        saveFloralArrangementPort.incrementViews(fa.id)
        return FloralArrangementDetailDto(
            id = fa.id,
            name = fa.name,
            slug = fa.slug,
            seoName = fa.seoName,
            price = fa.price.toPriceSummaryDto(),
            isAvailable = fa.isAvailable,
            seasonal = fa.seasonal,
            featured = fa.featured,
            views = fa.views,
            description = fa.description?.let {
                DescriptionDto(
                    shortDescription = it.shortDescription,
                    description = it.description
                )
            },
            gallery = fa.gallery.sortedBy {
                it.position
            }
                .map { it.toImageDto() },
            flowers = fa.flowers.map { f ->
                FlowerDto(
                    id = f.id,
                    name = f.name,
                    meaning = f.meaning
                )
            },
            categories = fa.categories.map { c ->
                CategorySummaryDto(
                    id = c.id!!, name = c.name, slug = c.slug, path = c.path
                )
            },
            tags = fa.tags.map { t ->
                TagSummaryDto(
                    id = t.id!!,
                    text = t.text,
                    route = t.route
                )
            },
            createdAt = fa.createdAt,
            updatedAt = fa.updatedAt
        )
    }

    private fun Price.toPriceSummaryDto(): PriceSummaryDto {
        val discountPercent = discountPrice?.let { dp ->
            price.subtract(dp)
                .divide(price, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toInt()
        }
        return PriceSummaryDto(
            amount = price,
            discountAmount = discountPrice,
            currency = currency,
            hasDiscount = discountPrice != null,
            discountPercent = discountPercent
        )
    }

    private fun ProductGallery.toImageDto() = ImageDto(
        publicId = publicId,
        originalUrl = originalUrl,
        thumbnailUrl = thumbnailUrl,
        mediumUrl = mediumUrl,
        altText = altText,
        isPrimary = isPrimary,
        position = position
    )
}
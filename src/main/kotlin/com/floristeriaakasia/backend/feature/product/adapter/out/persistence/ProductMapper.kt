package com.floristeriaakasia.backend.feature.product.adapter.out.persistence

import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain
import com.floristeriaakasia.backend.feature.product.domain.model.Money
import com.floristeriaakasia.backend.feature.product.domain.model.ProductGalleryDomain
import com.floristeriaakasia.backend.feature.product.domain.model.StockStatus
import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductGallery
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toDomain(entity: FloralArrangement): FloralArrangementDomain {
        val domain = FloralArrangementDomain(
            id = entity.id,
            name = entity.name,
            slug = entity.slug,
            categoryId = entity.categoryId,
            price = Money(entity.priceAmount, entity.currency),
            stockStatus = StockStatus.valueOf(entity.stockStatus),
            seasonal = entity.seasonal,
            featured = entity.featured
        )

        entity.gallery.forEach { galleryEntity ->
            domain.gallery.add(
                ProductGalleryDomain(
                    id = galleryEntity.id,
                    publicId = galleryEntity.publicId,
                    originalUrl = galleryEntity.originalUrl,
                    thumbnailUrl = galleryEntity.thumbnailUrl,
                    mediumUrl = galleryEntity.mediumUrl,
                    altText = galleryEntity.altText,
                    isPrimary = galleryEntity.isPrimary,
                    position = galleryEntity.position
                )
            )
        }
        return domain
    }

    fun toEntity(domain: FloralArrangementDomain): FloralArrangement {
        val entity = FloralArrangement(
            id = domain.id ?: 0,
            name = domain.name,
            slug = domain.slug,
            categoryId = domain.categoryId,
            priceAmount = domain.price.amount,
            currency = domain.price.currency,
            stockStatus = domain.stockStatus.name,
            seasonal = domain.seasonal,
            featured = domain.featured
        )


        domain.gallery.forEach { galleryDomain ->
            val galleryEntity = ProductGallery(
                id = galleryDomain.id ?: 0,
                publicId = galleryDomain.publicId,
                originalUrl = galleryDomain.originalUrl,
                thumbnailUrl = galleryDomain.thumbnailUrl,
                mediumUrl = galleryDomain.mediumUrl,
                altText = galleryDomain.altText,
                isPrimary = galleryDomain.isPrimary,
                position = galleryDomain.position
            )
            entity.addGalleryEntity(galleryEntity)
        }

        return entity
    }
}
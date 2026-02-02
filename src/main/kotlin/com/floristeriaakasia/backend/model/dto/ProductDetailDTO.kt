package com.floristeriaakasia.backend.model.dto

import com.floristeriaakasia.backend.model.*
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

data class ProductListDTO(
    val id: Long,
    val title: String,
    val slug: String,
    val price: BigDecimal,
    val stockStatus: String,
    val primaryImage: String?,
    val category: CategorySimpleDTO,
    val subCategory: SubCategorySimpleDTO,
    val featured: Boolean,
    val seasonal: Boolean
) {
    companion object {
        fun from(product: Product): ProductListDTO {
            return ProductListDTO(
                id = product.id!!,
                title = product.title,
                slug = product.slug,
                price = product.price,
                stockStatus = product.stockStatus.name,
                primaryImage = product.gallery.find { it.isPrimary }?.storedName,
                category = CategorySimpleDTO.from(product.category),
                subCategory = SubCategorySimpleDTO.from(product.subCategory),
                featured = product.featured,
                seasonal = product.seasonal
            )
        }
    }
}

data class ProductDetailDTO(
    val id: Long,
    val title: String,
    val slug: String,
    val fullPath: String,
    val price: BigDecimal,
    val stockStatus: String,
    val category: CategorySimpleDTO,
    val subCategory: SubCategorySimpleDTO,
    val tags: List<TagDTO>,
    val descriptions: List<ProductDescriptionDTO>,
    val gallery: List<ProductGalleryDTO>,
    val variants: List<ProductVariantDTO>,
    val featured: Boolean,
    val seasonal: Boolean,
    val facebookUrl: String?,
    val instagramUrl: String?,
    val views: Int,
    val createdAt: Instant,
    val updatedAt: Instant
){
    companion object{
        fun from(product: Product): ProductDetailDTO{
            return ProductDetailDTO(
                id = product.id!!,
                title = product.title,
                slug = product.slug,
                fullPath = product.getFullPath(),
                price = product.price,
                stockStatus = product.stockStatus.name,
                category = CategorySimpleDTO.from(product.category),
                subCategory = SubCategorySimpleDTO.from(product.subCategory),
                tags = product.tags.map { TagDTO.from(it) },
                descriptions = product.descriptions
                    .sortedBy { it.position }
                    .map { ProductDescriptionDTO.from(it) },
                gallery = product.gallery
                    .sortedBy { it.position }
                    .map { ProductGalleryDTO.from(it) },
                variants = product.variants
                    .sortedBy { it.position }
                    .map { ProductVariantDTO.from(it) },
                featured = product.featured,
                seasonal = product.seasonal,
                facebookUrl = product.facebookUrl,
                instagramUrl = product.instagramUrl,
                views = product.views,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

data class CategorySimpleDTO(
    val id: Long,
    val name: String,
    val route: String
) {
    companion object {
        fun from(category: Category) = CategorySimpleDTO(
            id = category.id!!,
            name = category.text,
            route = category.route
        )
    }
}

data class SubCategorySimpleDTO(
    val id: Long,
    val name: String,
    val route: String
) {
    companion object {
        fun from(subCategory: SubCategory) = SubCategorySimpleDTO(
            id = subCategory.id!!,
            name = subCategory.text,
            route = subCategory.route
        )
    }
}

data class TagDTO(
    val id: Long,
    val name: String,
    val route: String
) {
    companion object {
        fun from(tag: Tag) = TagDTO(
            id = tag.id!!,
            name = tag.text,
            route = tag.route
        )
    }
}

data class ProductDescriptionDTO(
    val id: Long,
    val paragraph: String,
    val position: Int
) {
    companion object {
        fun from(desc: ProductDescription) = ProductDescriptionDTO(
            id = desc.id,
            paragraph = desc.paragraph,
            position = desc.position
        )
    }
}

data class ProductGalleryDTO(
    val id: Long,
    val originalName: String,
    val url: String,
    val altText: String,
    val isPrimary: Boolean,
    val position: Int,
    val seasonal: Boolean
) {
    companion object {
        fun from(gallery: ProductGallery) = ProductGalleryDTO(
            id = gallery.id,
            originalName = gallery.originalName,
            url = gallery.storedName,  // Cloudinary URL
            altText = gallery.altText,
            isPrimary = gallery.isPrimary,
            position = gallery.position,
            seasonal = gallery.seasonal
        )
    }
}

data class ProductVariantDTO(
    val id: Long,
    val variantType: String,
    val name: String,
    val priceAdjustment: BigDecimal,
    val description: String,
    val position: Int,
    val available: Boolean
) {
    companion object {
        fun from(variant: ProductVariant) = ProductVariantDTO(
            id = variant.id,
            variantType = variant.variantType,
            name = variant.name,
            priceAdjustment = variant.priceAdjustment,
            description = variant.description,
            position = variant.position,
            available = variant.available
        )
    }
}

data class DescriptionCreateRequest(
    @field:NotBlank
    val paragraph: String,
    val position: Int? = null
)


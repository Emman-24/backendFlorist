package com.floristeriaakasia.backend.feature.product.domain.model

class FloralArrangementDomain(
    val id: Long? = null,
    var name: String,
    var slug: String,
    val categoryId: Long,
    val price: Money,
    var discountPrice: Money? = null,
    var stockStatus: StockStatus,
    var description: ProductDescriptionDomain? = null,
    val gallery: MutableList<ProductGalleryDomain> = mutableListOf(),
    val tags: MutableSet<TagDomain> = mutableSetOf(),
    var seasonal: Boolean = false,
    var featured: Boolean = false,
) {
    fun addImage(image: ProductGalleryDomain) {
        if (gallery.isEmpty()) {
            image.isPrimary = true
        }
        gallery.add(image)
    }

    fun removeImage(publicId: String) {
        gallery.removeIf { it.publicId == publicId }
        // Ensure there's always a primary image if gallery isn't empty
        if (gallery.isNotEmpty() && gallery.none { it.isPrimary }) {
            gallery.first().isPrimary = true
        }
    }
}

enum class StockStatus { AVAILABLE, SEASONAL, OUT_OF_STOCK }
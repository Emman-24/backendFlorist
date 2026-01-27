package com.floristeriaakasia.backend.controller.api


import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.service.ProductDetailsDTO
import com.floristeriaakasia.backend.service.ProductSeoService
import com.floristeriaakasia.backend.service.ProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val productSeoService: ProductSeoService
) {

    @GetMapping
    fun getAllProducts(): ResponseEntity<List<ProductListDTO>> {
        val products = productService.findAllActive()
        val dtos = products.map { ProductListDTO.from(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long
    ): ResponseEntity<ProductDetailDTO> {
        val details = productService.getProductWithDetails(id) ?: return ResponseEntity.notFound().build()
        productService.incrementViews(id)
        return ResponseEntity.ok(ProductDetailDTO.from(details, productSeoService))
    }

    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @PathVariable categoryId: Long
    ): ResponseEntity<List<ProductListDTO>> {
        val products = productService.findByCategory(categoryId)
        val dtos = products.map { ProductListDTO.from(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/subcategory/{subCategoryId}")
    fun getProductsBySubCategory(
        @PathVariable subCategoryId: Long
    ): ResponseEntity<List<ProductListDTO>> {
        val products = productService.findBySubCategory(subCategoryId)
        val dtos = products.map { ProductListDTO.from(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/featured")
    fun getFeaturedProducts(): ResponseEntity<List<ProductListDTO>> {
        val products = productService.findFeatured()
        val dtos = products.map { ProductListDTO.from(it) }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/route/{route}")
    fun getProductsByRoute(
        @PathVariable route: String
    ): ResponseEntity<ProductDetailDTO> {
        val product = productService.findByRoute(route) ?: return ResponseEntity.notFound().build()
        val details = productService.getProductWithDetails(product.id!!) ?: return ResponseEntity.notFound().build()
        productService.incrementViews(product.id)
        return ResponseEntity.ok(ProductDetailDTO.from(details, productSeoService))
    }


}

data class SubCategoryDTO(
    val id: Long,
    val name: String,
    val route: String,
    val description: String? = null,
    val position: Int? = null,
    val status: Boolean? = null
)
data class GalleryItemDTO(val id: Long, val url: String, val altText: String, val isPrimary: Boolean)
data class VariantDTO(
    val id: Long,
    val type: String,
    val name: String,
    val priceAdjustment: String,
    val description: String
)

data class TagDTO(val id: Long, val name: String)
data class ReviewDTO(
    val id: Long,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
)

data class SeoMetadataDTO(val title: String, val description: String, val keywords: String?)


data class ProductDetailDTO(
    val id: Long,
    val title: String,
    val route: String,
    val price: String,
    val stockStatus: String,
    val category: CategoryDTO,
    val subCategory: SubCategoryDTO,
    val descriptions: List<String>,
    val gallery: List<GalleryItemDTO>,
    val variants: List<VariantDTO>,
    val tags: List<TagDTO>,
    val reviews: List<ReviewDTO>,
    val averageRating: Double,
    val reviewCount: Long,
    val featured: Boolean,
    val seasonal: Boolean,
    val facebookUrl: String?,
    val instagramUrl: String?,
    val seoMetadata: SeoMetadataDTO?
) {
    companion object {
        fun from(details: ProductDetailsDTO, productSeoService: ProductSeoService): ProductDetailDTO {
            val seoMetadata = productSeoService.getOrGenerateMetadata(
                "product",
                details.product.id!!,
                details.product
            )

            return ProductDetailDTO(
                id = details.product.id,
                title = details.product.title,
                route = details.product.route,
                price = details.product.price.toString(),
                stockStatus = details.product.stockStatus,
                category = CategoryDTO(
                    id = details.product.category.id!!,
                    name = details.product.category.text,
                    route = details.product.category.route,
                    description = details.product.category.description,
                    subCategories = emptyList()
                ),
                subCategory = SubCategoryDTO(
                    id = details.product.subCategory.id!!,
                    name = details.product.subCategory.text,
                    route = details.product.subCategory.route
                ),
                descriptions = details.descriptions.map { it.paragraph },
                gallery = details.gallery.map {
                    GalleryItemDTO(
                        id = it.id,
                        url = it.storedName,
                        altText = it.altText,
                        isPrimary = it.isPrimary
                    )
                },
                variants = details.variants.map {
                    VariantDTO(
                        id = it.id,
                        type = it.variantType,
                        name = it.name,
                        priceAdjustment = it.priceAdjustment.toString(),
                        description = it.description
                    )
                },
                tags = details.tags.map { TagDTO(it.id!!, it.text) },
                reviews = details.reviews.map {
                    ReviewDTO(
                        id = it.id,
                        customerName = it.customerName,
                        rating = it.rating,
                        comment = it.comment,
                        createdAt = it.createdAt.toString()
                    )
                },
                averageRating = details.averageRating,
                reviewCount = details.reviewCount,
                featured = details.product.featured,
                seasonal = details.product.seasonal,
                facebookUrl = details.product.facebookUrl,
                instagramUrl = details.product.instagramUrl,
                seoMetadata = seoMetadata?.let {
                    SeoMetadataDTO(
                        title = it.metaTitle,
                        description = it.metaDescription,
                        keywords = it.metaKeywords
                    )
                }
            )
        }
    }
}

data class ProductListDTO(
    val id: Long,
    val title: String,
    val route: String,
    val price: String,
    val stockStatus: String,
    val categoryName: String,
    val subCategoryName: String,
    val primaryImage: String?,
    val featured: Boolean,
    val seasonal: Boolean,
    val averageRating: Double?,
    val reviewCount: Int
) {
    companion object {
        fun from(product: Product): ProductListDTO {
            return ProductListDTO(
                id = product.id!!,
                title = product.title,
                route = product.route,
                price = product.price.toString(),
                stockStatus = product.stockStatus,
                categoryName = product.category.text,
                subCategoryName = product.subCategory.text,
                primaryImage = product.gallery.find { it.isPrimary }?.storedName,
                featured = product.featured,
                seasonal = product.seasonal,
                averageRating = null, // TODO: calcular desde reviews
                reviewCount = 0 // TODO: contar reviews aprobados
            )
        }
    }
}

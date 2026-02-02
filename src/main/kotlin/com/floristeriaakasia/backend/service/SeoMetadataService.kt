package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.ProductGallery
import com.floristeriaakasia.backend.model.SeoMetadata
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductGalleryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.SeoMetadataRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SeoMetadataService(
    private val seoMetadataRepository: SeoMetadataRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubcategoryRepository,
    private val productGalleryRepository: ProductGalleryRepository
) {
    fun generateProductMetadata(product: Product): SeoMetadata {

        val existing = seoMetadataRepository.findByEntityTypeAndEntityId("product", product.id!!)

        val metaTitle = "${product.title} - Floristería Akasia Pereira"
        val metaDescription = "Compra ${product.title} en Floristería Akasia. " +
                "Arreglos florales frescos con entrega en Pereira, Dosquebradas y La Virginia. " +
                "Precio: $${product.price}"

        val ogTitle = product.title

        val gallery: ProductGallery? = productGalleryRepository.findByProductAndIsPrimaryTrue(product)

        val ogDescription = "Hermoso arreglo floral ${product.title} disponible en Floristería Akasia Pereira."

        val schemaMarkup = """
        {
          "@context": "https://schema.org/",
          "@type": "Product",
          "name": "${product.title}",
          "image": "https://backend.floristeriaakasia.com.co/${gallery?.storedName}",
          "description": "$metaDescription",
          "brand": {
            "@type": "Brand",
            "name": "Floristería Akasia"
          },
          "offers": {
            "@type": "Offer",
            "url": "https://www.floristeriaakasia.com.co/productos/${product.slug}",
            "priceCurrency": "COP",
            "price": "${product.price}",
            "availability": "https://schema.org/InStock"
          }
        }
        """.trimIndent()
        return if (existing != null) {
            existing.apply {
                this.metaTitle = metaTitle
                this.metaDescription = metaDescription
                this.schemaMarkup = schemaMarkup
            }
            seoMetadataRepository.save(existing)
        } else {
            seoMetadataRepository.save(
                SeoMetadata(
                    entityType = "product",
                    entityId = product.id,
                    metaTitle = metaTitle,
                    metaDescription = metaDescription,
                    schemaMarkup = schemaMarkup
                )
            )
        }
    }

    fun getOrGenerateMetadata(entityType: String, entityId: Long): SeoMetadata? {
        val existing = seoMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId)
        if (existing != null) return existing
        return when (entityType) {
            "product" -> {
                val product = productRepository.findById(entityId).orElse(null)
                product?.let { generateProductMetadata(it) }
            }

            else -> null
        }
    }
}
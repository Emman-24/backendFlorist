package com.floristeriaakasia.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SeoMetadata
import com.floristeriaakasia.backend.repository.ProductGalleryRepository
import com.floristeriaakasia.backend.repository.SeoMetadataRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSeoService(
    private val seoUrlService: SeoUrlService,
    private val seoMetadataRepository: SeoMetadataRepository,
    private val productGalleryRepository: ProductGalleryRepository,
    private val objectMapper: ObjectMapper,

    @Value("\${app.base-url:https://www.floristeriaakasia.com.co}")
    private val baseUrl: String,

    @Value("\${app.backend-url:https://backend.floristeriaakasia.com.co}")
    private val backendUrl: String
) {

    @Transactional
    fun createOrUpdateProductSeo(product: Product) {
        seoUrlService.createOrUpdateProductUrl(product)
        generateProductMetadata(product)
    }

    @Transactional
    fun generateProductMetadata(product: Product): SeoMetadata {
        val existing = seoMetadataRepository.findByEntityTypeAndEntityId("product", product.id!!)

        val primaryImage = productGalleryRepository.findByProductAndIsPrimaryTrue(product)

        val imageUrl =
            primaryImage?.let { "$backendUrl/${it.storedName}" } ?: "$backendUrl/images/placeholder-product.jpg"

        val metaTitle = buildMetaTitle(product)
        val metaDescription = buildMetaDescription(product)
        val schemaMarkup = buildSchemaMarkup(product, imageUrl)

        return if (existing != null) {

            if (existing.isCustom) {
                existing.apply {
                    this.metaTitle = metaTitle
                    this.metaDescription = metaDescription
                    this.schemaMarkup = schemaMarkup
                }
                seoMetadataRepository.save(existing)
            } else {
                existing
            }
        } else {
            seoMetadataRepository.save(
                SeoMetadata(
                    entityType = "product",
                    entityId = product.id,
                    metaTitle = metaTitle,
                    metaDescription = metaDescription,
                    schemaMarkup = schemaMarkup,
                    isCustom = false
                )
            )
        }

    }

    @Transactional
    fun setCustomMetadata(
        product: Product,
        title: String?,
        description: String?,
        keywords: String?,
        ogTitle: String? = null,
        ogDescription: String? = null
    ):SeoMetadata{
        val existing = seoMetadataRepository.findByEntityTypeAndEntityId("product", product.id!!)
        val primaryImage = productGalleryRepository.findByProductAndIsPrimaryTrue(product)
        val imageUrl = primaryImage?.let { "$backendUrl/${it.storedName}" } ?: "$backendUrl/images/placeholder-product.jpg"

        return if (existing != null) {

            existing.apply {
                title?.let { this.metaTitle = it }
                description?.let { this.metaDescription = it }
                this.isCustom = true
                this.schemaMarkup = buildSchemaMarkup(product, imageUrl)
            }

            seoMetadataRepository.save(existing)

        } else {

            seoMetadataRepository.save(
                SeoMetadata(
                    entityType = "product",
                    entityId = product.id,
                    metaTitle = title ?: buildMetaTitle(product),
                    metaDescription = description ?: buildMetaDescription(product),
                    schemaMarkup = buildSchemaMarkup(product, imageUrl),
                    isCustom = true
                )
            )

        }
    }


    @Transactional
    fun regenerateUrl(product: Product) {
        seoUrlService.createOrUpdateProductUrl(product)
    }

    private fun buildMetaTitle(product: Product): String {
        return "${product.title} - Floristería Akasia Pereira | Entrega a Domicilio"
    }

    private fun buildMetaDescription(product: Product): String {
        val category = product.category.text
        val price = product.price

        return "Compra ${product.title} en Floristería Akasia. " +
                "Arreglos florales de $category frescos con entrega en Pereira, " +
                "Dosquebradas y La Virginia. Desde $$price COP. ¡Ordena ahora!"
    }

    private fun buildOgDescription(product: Product): String {
        return "Hermoso arreglo floral ${product.title} disponible en " +
                "Floristería Akasia Pereira. Entrega el mismo día."
    }

    private fun buildSchemaMarkup(product: Product, imageUrl: String): String {
        val productUrl = seoUrlService.generateProductFullPath(product)

        val schema = mutableMapOf(
            "@context" to "https://schema.org/",
            "@type" to "Product",
            "name" to escapeJson(product.title),
            "image" to imageUrl,
            "description" to escapeJson(buildMetaDescription(product)),
            "brand" to mapOf(
                "@type" to "Brand",
                "name" to "Floristería Akasia"
            ),
            "offers" to mapOf(
                "@type" to "Offer",
                "url" to "$baseUrl$productUrl",
                "priceCurrency" to "COP",
                "price" to product.price.toString(),
                "availability" to "https://schema.org/InStock"
            )
        )

        return try {
            objectMapper.writeValueAsString(schema)
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    @Transactional
    fun deleteProductSeo(product: Product) {
        seoUrlService.deleteProductUrl(product)
        seoMetadataRepository.findByEntityTypeAndEntityId("product", product.id!!)?.let {
            seoMetadataRepository.delete(it)
        }
    }

    @Transactional(readOnly = true)
    fun getOrGenerateMetadata(entityType: String, entityId: Long, product: Product? = null): SeoMetadata? {
        val existing = seoMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId)
        if (existing != null) return existing

        return when (entityType) {
            "product" -> product?.let { generateProductMetadata(it) }
            else -> null
        }
    }
}
package com.floristeriaakasia.backend.model.dto.product

import com.floristeriaakasia.backend.model.Category
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.SubCategory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.text.Normalizer

@Component
class ProductMapper {

    fun toResponse(product: Product): ProductResponse {
        return ProductResponse(
            id = product.id!!,
            text = product.title,
            categoryName = product.category.text,
            subcategoryName = product.subCategory.text,
            price = product.price.toInt(),
            status = product.status,
            imageUrl = product.storedName,
            createdAt = product.createdAt.toString()
        )

    }

    fun toCreateRequest(product: Product): ProductCreateRequest {
        return ProductCreateRequest(
            text = product.title,
            route = product.route,
            status = product.status,
            categoryId = product.category.id!!,
            subCategoryId = product.subCategory.id!!,
            price = product.price,
            description = product.description?.joinToString("\n") ?: "",
            facebookUrl = product.facebookUrl,
            instagramUrl = product.instagramUrl,
            tagIds = product.tags.mapNotNull { it.id }.toMutableList()
        )
    }

    fun toEntity(
        request: ProductCreateRequest,
        parentCategory: Category,
        parentSubcategory: SubCategory,
        file: MultipartFile,
        storagePath: String
    ): Product {

        // Map single textarea description to a list of paragraphs
        val paragraphs: MutableList<String>? = request.description
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
            .let { if (it.isEmpty()) null else it }

        val product = Product(
            title = request.text,
            route = slugify(request.route),
            status = request.status ?: true,
            price = request.price!!,
            originalName = file.originalFilename ?: "",
            storedName = storagePath,
            mimeType = file.contentType ?: "application/octet-stream",
            size = file.size,
            facebookUrl = request.facebookUrl ?: "",
            instagramUrl = request.instagramUrl ?: ""
        )

        product.category = parentCategory
        product.subCategory = parentSubcategory
        product.description = paragraphs

        return product
    }

    private fun slugify(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return normalized
            .lowercase()
            .replace("[^a-z0-9]+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
            .trim('-')
    }


}
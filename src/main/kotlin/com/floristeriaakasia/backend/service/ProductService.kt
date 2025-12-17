package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.dto.product.ProductCreateRequest
import com.floristeriaakasia.backend.model.dto.product.ProductMapper
import com.floristeriaakasia.backend.model.dto.product.ProductResponse
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.ProductRepository
import com.floristeriaakasia.backend.repository.TagRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer

@Service
class ProductService(
    private val properties: ImageStorageProperties,
    private val repository: ProductRepository,
    private val storageService: LocalImageStorageService,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository,
    private val tagRepository: TagRepository,
    private val mapper: ProductMapper
) {

    @Transactional(readOnly = true)
    fun findAll(): List<ProductResponse> {
        return repository.findAll().map(mapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun findByStatus(status: Boolean, page: Int, size: Int): Page<ProductResponse> {
        val pageable = PageRequest.of(page, size)
        return repository.findByStatus(status, pageable).map(mapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun finByRequestId(id: Long): ProductCreateRequest {
        return repository.findById(id).map(mapper::toCreateRequest).orElseThrow {
            ResourceNotFoundException("Product with id $id not found")
        }
    }

    fun update(
        id: Long, request: ProductCreateRequest,
        file: MultipartFile
    ): Product {
        val existingProduct: Product = repository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Product with id $id not found")


        val parentCategory = categoryRepository.findById(request.categoryId).orElseThrow {
            IllegalArgumentException("Category not found")
        }
        val parentSubcategory = subcategoryRepository.findById(request.subCategoryId).orElseThrow {
            IllegalArgumentException("Subcategory not found")
        }


        existingProduct.apply {
            category = parentCategory
            subCategory = parentSubcategory
            route = slugify(request.route)
            status = request.status ?: true
            title = request.text


            description = request.description
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableList()
                .let { if (it.isEmpty()) null else it }

            price = request.price!!
            facebookUrl = request.facebookUrl
            instagramUrl = request.instagramUrl
        }

        // Update tags
        val tags = if (request.tagIds.isNotEmpty()) tagRepository.findAllById(request.tagIds) else emptyList()
        existingProduct.tags = tags.toMutableList()


        if (!file.isEmpty) {
            validateImage(file)
            val oldStoredName = existingProduct.storedName

            val newStoragePath = file.inputStream.use { inputStream ->
                storageService.storeFile(
                    inputStream,
                    file.originalFilename ?: "upload",
                    parentCategory.text,
                    parentSubcategory.text
                )
            }

            existingProduct.apply {
                originalName = file.originalFilename ?: ""
                storedName = newStoragePath
                mimeType = file.contentType ?: "application/octet-stream"
                size = file.size
            }

            return try {
                val saved = repository.save(existingProduct)

                if (oldStoredName.isNotBlank() && oldStoredName != newStoragePath) {
                    storageService.deleteFile(oldStoredName)
                }
                saved
            } catch (ex: Exception) {
                storageService.deleteFile(newStoragePath)
                throw ex
            }
        }

        return repository.save(existingProduct)
    }

    fun create(
        request: ProductCreateRequest,
        file: MultipartFile
    ): Product {
        validateImage(file)

        val categoryId = request.categoryId
        val subcategoryId = request.subCategoryId

        val parentCategory = categoryRepository.findById(categoryId).orElseThrow {
            IllegalArgumentException("Category not found")
        }
        val parentSubcategory = subcategoryRepository.findById(subcategoryId).orElseThrow {
            IllegalArgumentException("Subcategory not found")
        }
        val storagePath = file.inputStream.use { inputStream ->
            storageService.storeFile(
                inputStream,
                file.originalFilename ?: "upload",
                parentCategory.text,
                parentSubcategory.text
            )
        }

        val product = mapper.toEntity(
            request,
            parentCategory,
            parentSubcategory,
            file, storagePath
        )

        // Set tags for new product
        val tags = if (request.tagIds.isNotEmpty()) tagRepository.findAllById(request.tagIds) else emptyList()
        product.tags = tags.toMutableList()

        return repository.save(product)
    }


    fun deleteById(id: Long) {
        val product: Product =
            repository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Product with id $id not found")
        val storedName = product.storedName
        repository.deleteById(id)
        if (storedName.isNotBlank()) {
            storageService.deleteFile(storedName)
        }
    }

    private fun validateImage(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty.")
        }
        val mimeType = file.contentType

        if (mimeType == null || !properties.allowedMimeTypes.contains(mimeType)) {
            throw IllegalArgumentException("Invalid mime type")
        }
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
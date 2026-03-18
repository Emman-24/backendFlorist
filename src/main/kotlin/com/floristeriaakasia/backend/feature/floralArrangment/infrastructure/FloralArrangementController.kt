package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.application.AddImageToFloralArrangementUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementCommand
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.application.GetFloralArrangementsUseCase
import com.floristeriaakasia.backend.global.exeption.FloralArrangementNotFoundException
import com.floristeriaakasia.backend.util.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

@RestController
@RequestMapping("/api/floral-arrangement")
class FloralArrangementController(
    private val createFloralArrangementUseCase: CreateFloralArrangementUseCase,
    private val addImageToFloralArrangementUseCase: AddImageToFloralArrangementUseCase,
    private val getFloralArrangementsUseCase: GetFloralArrangementsUseCase
) {

    @GetMapping
    fun getArrangements(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        @RequestParam(defaultValue = "views") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
        @RequestParam categoryId: Long? = null,
        @RequestParam tagId: Long? = null,
        @RequestParam featured: Boolean? = null,
        @RequestParam seasonal: Boolean? = null,
        @RequestParam available: Boolean? = null,
        @RequestParam minPrice: BigDecimal? = null,
        @RequestParam maxPrice: BigDecimal? = null,
    ): ResponseEntity<Page<FloralArrangementSummaryDto>?> {

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 50)
        val direction = if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val safeSortBy = ALLOWED_SORT_FIELDS.getOrDefault(sortBy, "views")
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(direction, safeSortBy))

        val query = FloralArrangementQuery(
            categoryId = categoryId,
            tagId = tagId,
            featured = featured,
            seasonal = seasonal,
            isAvailable = available,
            minPrice = minPrice,
            maxPrice = maxPrice
        )
        val resultPage: Page<FloralArrangementSummaryDto> = getFloralArrangementsUseCase.execute(query, pageable)

        return ResponseEntity.ok(resultPage)
    }

    @GetMapping("/{id}")
    fun getArrangementById(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<FloralArrangementDetailDto>> {
        return try {
            val dto = getFloralArrangementsUseCase.executeById(id)
            ResponseEntity.ok(ApiResponse.Success(data = dto))
        } catch (e: FloralArrangementNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.Error(message = e.message ?: "Arrangement not found"))
        }
    }

    @PostMapping
    fun createArrangement(
        @Valid @RequestBody request: CreateFloralArrangementRequest
    ): ResponseEntity<String> {
        return try {
            val command = mapToCommand(request)
            createFloralArrangementUseCase.execute(command)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PostMapping("/{floralArrangementId}/image")
    fun uploadFloralArrangementImage(
        @PathVariable floralArrangementId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("altText") altText: String
    ): ResponseEntity<String> {
        return try {
            addImageToFloralArrangementUseCase.execute(floralArrangementId, file, altText)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }


    private fun mapToCommand(req: CreateFloralArrangementRequest) =
        CreateFloralArrangementCommand(
            name = req.name,
            seoName = req.seoName,
            categoryIds = req.categoryIds,
            priceAmount = req.price,
            discountPriceAmount = req.discountPrice,
            currency = req.currency,
            isAvailable = req.isAvailable,
            seasonal = req.seasonal,
            featured = req.featured,
            description = req.description,
            shortDescription = req.shortDescription,
            flowers = req.flowers.map {
                CreateFloralArrangementCommand.FlowerData(
                    name = it.name,
                    meaning = it.meaning
                )
            },
            tagIds = req.tagIds
        )

    companion object {
        private val ALLOWED_SORT_FIELDS = mapOf(
            "name" to "name",
            "price" to "price",
            "views" to "views",
            "createdAt" to "createdAt",
            "updatedAt" to "updatedAt"
        )
    }
}
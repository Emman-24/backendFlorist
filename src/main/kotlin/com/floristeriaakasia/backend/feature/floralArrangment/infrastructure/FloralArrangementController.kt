package com.floristeriaakasia.backend.feature.floralArrangment.infrastructure

import com.floristeriaakasia.backend.feature.floralArrangment.application.AddImageToFloralArrangementUseCase
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementCommand
import com.floristeriaakasia.backend.feature.floralArrangment.application.CreateFloralArrangementUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/floral-arrangement")
class FloralArrangementController(
    private val createFloralArrangementUseCase: CreateFloralArrangementUseCase,
    private val addImageToFloralArrangementUseCase: AddImageToFloralArrangementUseCase
) {
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
            slug = req.slug,
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

}
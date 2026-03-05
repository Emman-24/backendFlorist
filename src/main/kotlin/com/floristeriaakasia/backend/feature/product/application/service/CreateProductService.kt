package com.floristeriaakasia.backend.feature.product.application.service

import com.floristeriaakasia.backend.feature.product.adapter.out.persistence.ProductPersistenceAdapter
import com.floristeriaakasia.backend.feature.product.application.port.`in`.CreateProductCommand
import com.floristeriaakasia.backend.feature.product.application.port.`in`.CreateProductUseCase
import com.floristeriaakasia.backend.feature.product.application.port.out.SaveProductPort
import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain
import com.floristeriaakasia.backend.feature.product.domain.model.Money
import com.floristeriaakasia.backend.feature.product.domain.model.StockStatus
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.text.Normalizer
import java.util.*

@Service
class CreateProductService(
    private val productRepository: ProductPersistenceAdapter,
): CreateProductUseCase {

    @Transactional
    override fun execute(command: CreateProductCommand): FloralArrangementDomain {
        val generatedSlug = generateSlug(command.name)

        val newProduct = FloralArrangementDomain(
            name = command.name,
            slug = generatedSlug,
            categoryId = command.categoryId,
            price = Money(command.priceAmount, command.currency),
            discountPrice = command.discountPrice?.let { Money(it, command.currency) },
            stockStatus = StockStatus.AVAILABLE,
            seasonal = command.seasonal,
            featured = command.featured
        )

        return productRepository.save(newProduct)
    }
    private fun generateSlug(input: String): String {
        return Normalizer.normalize(input.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
            .replace("[^a-z0-9\\s-]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
    }
}
package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.category.infrastructure.api.LoadCategoryPort
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import com.floristeriaakasia.backend.feature.flowers.Flowers
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer
import java.util.Locale

@Service
@Transactional
class CreateFloralArrangementUseCaseImpl(
    private val saveFloralArrangementPort: SaveFloralArrangementPort,
    private val loadCategoryPort: LoadCategoryPort
) : CreateFloralArrangementUseCase {
    override fun execute(command: CreateFloralArrangementCommand): Long {

        val resolvedCategories = loadCategoryPort.loadAllByIds(command.categoryIds)
        val arrangement = buildArrangement(command)

        resolvedCategories.forEach { arrangement.categories.add(it) }

        val savedId = saveFloralArrangementPort.save(arrangement)
        return savedId
    }
}


private fun buildArrangement(cmd: CreateFloralArrangementCommand): FloralArrangement {
    val description = ProductDescription(
        shortDescription = cmd.shortDescription,
        description = cmd.description
    )

    val price = Price(
        price = cmd.priceAmount,
        discountPrice = cmd.discountPriceAmount
    )

    val arrangement = FloralArrangement(
        name = cmd.name,
        seoName = cmd.seoName,
        slug = generateSlug(cmd.name),
        price = price,
        isAvailable = cmd.isAvailable,
        seasonal = cmd.seasonal,
        featured = cmd.featured,
        description = description
    )

    cmd.flowers.forEach { flowerData ->
        val flower = Flowers(
            name = flowerData.name,
            meaning = flowerData.meaning,
            floralArrangement = arrangement
        )
        arrangement.flowers.add(flower)
    }

    return arrangement
}

private fun generateSlug(input: String): String {
    return Normalizer.normalize(input.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        .replace("[^a-z0-9\\s-]".toRegex(), "")
        .replace("\\s+".toRegex(), "-")
}
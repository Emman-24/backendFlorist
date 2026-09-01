package com.floristeriaakasia.backend.feature.floralArrangment.application

import com.floristeriaakasia.backend.feature.category.infrastructure.api.Category
import com.floristeriaakasia.backend.feature.category.infrastructure.api.LoadCategoryPort
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangement
import com.floristeriaakasia.backend.feature.floralArrangment.infrastructure.SaveFloralArrangementPort
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal

class CreateFloralArrangementUseCaseImplTest {

    private lateinit var saveFloralArrangementPort: SaveFloralArrangementPort
    private lateinit var loadCategoryPort: LoadCategoryPort
    private lateinit var useCase: CreateFloralArrangementUseCaseImpl

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObject(): T {
        any<T>()
        return null as T
    }

    @BeforeEach
    fun setUp() {
        saveFloralArrangementPort = mock(SaveFloralArrangementPort::class.java)
        loadCategoryPort = mock(LoadCategoryPort::class.java)
        useCase = CreateFloralArrangementUseCaseImpl(saveFloralArrangementPort, loadCategoryPort)
    }

    private fun createCommand(
        name: String = "Orquídeas",
        seoName: String = "orquideas",
        categoryIds: Set<Long> = setOf(1L),
        priceAmount: BigDecimal = BigDecimal("90000.00"),
        discountPriceAmount: BigDecimal? = null,
        currency: String = "COP",
        isAvailable: Boolean = true,
        seasonal: Boolean = false,
        featured: Boolean = false,
        shortDescription: String = "Orquídeas",
        description: String = "Orquídeas exóticas",
        flowers: List<CreateFloralArrangementCommand.FlowerData> = emptyList(),
        tagIds: Set<Long> = emptySet()
    ) = CreateFloralArrangementCommand(
        name = name,
        seoName = seoName,
        categoryIds = categoryIds,
        priceAmount = priceAmount,
        discountPriceAmount = discountPriceAmount,
        currency = currency,
        isAvailable = isAvailable,
        seasonal = seasonal,
        featured = featured,
        shortDescription = shortDescription,
        description = description,
        flowers = flowers,
        tagIds = tagIds
    )

    @Test
    fun `execute should create and save floral arrangement successfully and return generated id`() {
        val category1 = Category(id = 1L, name = "Romántico", slug = "romántico", path = "/1/", depth = 0)
        val category2 = Category(id = 2L, name = "Aniversario", slug = "aniversario", path = "/2/", depth = 0)

        `when`(loadCategoryPort.loadAllByIds(setOf(1L, 2L))).thenReturn(listOf(category1, category2))

        var savedArrangement: FloralArrangement? = null
        `when`(saveFloralArrangementPort.save(anyObject())).thenAnswer { invocation ->
            savedArrangement = invocation.getArgument(0) as FloralArrangement
            42L
        }

        val command = CreateFloralArrangementCommand(
            name = "Rosas Rojas & Lirios",
            seoName = "rosas-rojas-lirios",
            categoryIds = setOf(1L, 2L),
            priceAmount = BigDecimal("85000.00"),
            discountPriceAmount = BigDecimal("75000.00"),
            currency = "COP",
            isAvailable = true,
            seasonal = false,
            featured = true,
            shortDescription = "Elegante ramo de rosas.",
            description = "Elegante ramo de rosas rojas seleccionadas y lirios blancos aromáticos.",
            flowers = listOf(
                CreateFloralArrangementCommand.FlowerData(name = "Rosa Roja", meaning = "Amor y pasión"),
                CreateFloralArrangementCommand.FlowerData(name = "Lirio Blanco", meaning = "Pureza y elegancia")
            ),
            tagIds = setOf(10L)
        )

        val resultId = useCase.execute(command)

        assertEquals(42L, resultId)
        verify(loadCategoryPort).loadAllByIds(setOf(1L, 2L))

        assertNotNull(savedArrangement)
        val arrangement = savedArrangement!!
        assertEquals("Rosas Rojas & Lirios", arrangement.name)
        assertEquals("rosas-rojas-lirios", arrangement.seoName)
        assertEquals("rosas-rojas-lirios", arrangement.slug)
        assertTrue(arrangement.isAvailable)
        assertFalse(arrangement.seasonal)
        assertTrue(arrangement.featured)

        // Price assertions
        assertEquals(BigDecimal("85000.00"), arrangement.price.price)
        assertEquals(BigDecimal("75000.00"), arrangement.price.discountPrice)
        assertEquals("COP", arrangement.price.currency)

        // Description assertions
        assertNotNull(arrangement.description)
        assertEquals("Elegante ramo de rosas.", arrangement.description?.shortDescription)
        assertEquals("Elegante ramo de rosas rojas seleccionadas y lirios blancos aromáticos.", arrangement.description?.description)

        // Categories assertions
        assertEquals(2, arrangement.categories.size)
        assertTrue(arrangement.categories.contains(category1))
        assertTrue(arrangement.categories.contains(category2))

        // Flowers assertions
        assertEquals(2, arrangement.flowers.size)
        val flowerNames = arrangement.flowers.map { it.name }
        assertTrue(flowerNames.contains("Rosa Roja"))
        assertTrue(flowerNames.contains("Lirio Blanco"))
        assertTrue(arrangement.flowers.all { it.floralArrangement === arrangement })
    }

    @Test
    fun `execute should generate correct slug handling Spanish accents and special characters`() {
        val category = Category(id = 1L, name = "Bodas", slug = "bodas", path = "/1/", depth = 0)
        `when`(loadCategoryPort.loadAllByIds(setOf(1L))).thenReturn(listOf(category))

        var savedArrangement: FloralArrangement? = null
        `when`(saveFloralArrangementPort.save(anyObject())).thenAnswer { invocation ->
            savedArrangement = invocation.getArgument(0) as FloralArrangement
            1L
        }

        val command = createCommand(
            name = "¡Arreglo Especial Navideño Año 2026!",
            seoName = "arreglo-especial-navideno-ano-2026",
            shortDescription = "Navidad",
            description = "Arreglo navideño"
        )

        useCase.execute(command)

        assertNotNull(savedArrangement)
        assertEquals("arreglo-especial-navideno-ano-2026", savedArrangement?.slug)
    }

    @Test
    fun `execute should handle arrangement without flowers or discount price`() {
        val category = Category(id = 5L, name = "General", slug = "general", path = "/5/", depth = 0)
        `when`(loadCategoryPort.loadAllByIds(setOf(5L))).thenReturn(listOf(category))

        var savedArrangement: FloralArrangement? = null
        `when`(saveFloralArrangementPort.save(anyObject())).thenAnswer { invocation ->
            savedArrangement = invocation.getArgument(0) as FloralArrangement
            10L
        }

        val command = createCommand(
            name = "Girasoles Simples",
            seoName = "girasoles-simples",
            categoryIds = setOf(5L),
            priceAmount = BigDecimal("30000.00"),
            discountPriceAmount = null,
            currency = "USD",
            shortDescription = "Girasoles",
            description = "Girasoles frescos"
        )

        val resultId = useCase.execute(command)

        assertEquals(10L, resultId)
        assertNotNull(savedArrangement)
        val saved = savedArrangement!!
        assertNull(saved.price.discountPrice)
        assertEquals("COP", saved.price.currency)
        assertTrue(saved.flowers.isEmpty())
    }

    @Test
    fun `execute should propagate exception when loadCategoryPort fails`() {
        `when`(loadCategoryPort.loadAllByIds(setOf(1L))).thenThrow(RuntimeException("Database connection failed"))

        val command = createCommand()

        val ex = assertThrows<RuntimeException> {
            useCase.execute(command)
        }
        assertEquals("Database connection failed", ex.message)
        verifyNoInteractions(saveFloralArrangementPort)
    }

    @Test
    fun `execute should propagate exception when saveFloralArrangementPort fails`() {
        val category = Category(id = 1L, name = "Flores", slug = "flores", path = "/1/", depth = 0)
        `when`(loadCategoryPort.loadAllByIds(setOf(1L))).thenReturn(listOf(category))
        `when`(saveFloralArrangementPort.save(anyObject()))
            .thenThrow(IllegalStateException("Save error"))

        val command = createCommand()

        val ex = assertThrows<IllegalStateException> {
            useCase.execute(command)
        }
        assertEquals("Save error", ex.message)
    }
}

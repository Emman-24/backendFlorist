package com.floristeriaakasia.backend.feature.floralArrangment.domain

import com.floristeriaakasia.backend.config.RepositoryTestBase
import com.floristeriaakasia.backend.feature.category.Category
import com.floristeriaakasia.backend.feature.category.CategoryRepository
import com.floristeriaakasia.backend.feature.flowers.Flowers
import com.floristeriaakasia.backend.feature.price.Price
import com.floristeriaakasia.backend.feature.productDescription.ProductDescription
import com.floristeriaakasia.backend.feature.tag.Tag
import com.floristeriaakasia.backend.feature.tag.TagRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FloralArrangementRepositoryTest : RepositoryTestBase() {

    @Autowired
    private lateinit var floralArrangementRepository: FloralArrangementRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var tagRepository: TagRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var romanticCategory: Category
    private lateinit var birthdayCategory: Category

    private lateinit var roseTag: Tag
    private lateinit var tulipTag: Tag

    private lateinit var redRoseBouquet: FloralArrangement
    private lateinit var springTulips: FloralArrangement
    private lateinit var luxuryOrchids: FloralArrangement
    private lateinit var summerSunflowers: FloralArrangement

    @BeforeEach
    fun setUp() {
        romanticCategory = saveCategory(name = "Romantic", slug = "romantic")
        birthdayCategory = saveCategory(name = "Birthday", slug = "birthday")

        roseTag = saveTag(text = "Roses", route = "roses")
        tulipTag = saveTag(text = "Tulips", route = "tulips")

        redRoseBouquet = saveArrangement(
            name = "Red Rose Bouquet",
            seoName = "red-rose-bouquet",
            slug = "red-rose-bouquet",
            price = BigDecimal("75000.00"),
            isAvailable = true,
            seasonal = false,
            featured = true,
            views = 10,
            categories = setOf(romanticCategory, birthdayCategory),
            tags = setOf(roseTag),
            descriptionText = "A beautiful bouquet of premium red roses.",
            flowersList = listOf("Red Rose" to "Love and passion"),
            images = listOf(
                "img_rose_1" to true,
                "img_rose_2" to false
            )
        )

        springTulips = saveArrangement(
            name = "Spring Tulips",
            seoName = "spring-tulips",
            slug = "spring-tulips",
            price = BigDecimal("50000.00"),
            isAvailable = true,
            seasonal = true,
            featured = false,
            views = 5,
            categories = setOf(romanticCategory),
            tags = setOf(tulipTag),
            descriptionText = "Fresh spring tulips in various colors.",
            flowersList = listOf("Tulip" to "Perfect love"),
            images = listOf(
                "img_tulip_1" to true
            )
        )

        luxuryOrchids = saveArrangement(
            name = "Luxury Orchids",
            seoName = "luxury-orchids",
            slug = "luxury-orchids",
            price = BigDecimal("150000.00"),
            isAvailable = false,
            seasonal = false,
            featured = true,
            views = 20,
            categories = setOf(birthdayCategory),
            tags = emptySet(),
            descriptionText = "Exotic luxury orchids in a ceramic vase.",
            flowersList = listOf("Orchid" to "Luxury and strength"),
            images = listOf(
                "img_orchid_1" to true,
                "img_orchid_2" to false
            )
        )

        summerSunflowers = saveArrangement(
            name = "Summer Sunflowers",
            seoName = "summer-sunflowers",
            slug = "summer-sunflowers",
            price = BigDecimal("45000.00"),
            isAvailable = true,
            seasonal = true,
            featured = false,
            views = 0,
            categories = emptySet(),
            tags = emptySet(),
            descriptionText = null,
            flowersList = emptyList(),
            images = emptyList()
        )

        entityManager.flush()
        entityManager.clear()
    }

    // --- existsBySlug ---

    @Test
    fun `existsBySlug returns true when arrangement with slug exists`() {
        assertTrue(floralArrangementRepository.existsBySlug("red-rose-bouquet"))
        assertTrue(floralArrangementRepository.existsBySlug("spring-tulips"))
    }

    @Test
    fun `existsBySlug returns false when arrangement with slug does not exist`() {
        assertFalse(floralArrangementRepository.existsBySlug("non-existent-slug"))
        assertFalse(floralArrangementRepository.existsBySlug(""))
    }

    // --- findBySlug ---

    @Test
    fun `findBySlug returns the matching floral arrangement`() {
        val found = floralArrangementRepository.findBySlug("red-rose-bouquet")

        assertNotNull(found)
        assertEquals("Red Rose Bouquet", found.name)
        assertEquals(redRoseBouquet.id, found.id)
    }

    @Test
    fun `findBySlug returns null when slug does not exist`() {
        val found = floralArrangementRepository.findBySlug("non-existent-slug")
        assertNull(found)
    }

    // --- findBySeoName ---

    @Test
    fun `findBySeoName returns the matching floral arrangement`() {
        val found = floralArrangementRepository.findBySeoName("luxury-orchids")

        assertNotNull(found)
        assertEquals("Luxury Orchids", found.name)
        assertEquals(luxuryOrchids.id, found.id)
    }

    @Test
    fun `findBySeoName returns null when seoName does not exist`() {
        val found = floralArrangementRepository.findBySeoName("unknown-seo-name")
        assertNull(found)
    }

    // --- incrementViews ---

    @Test
    fun `incrementViews increments views count by 1 in database`() {
        val initialViews = floralArrangementRepository.findById(redRoseBouquet.id).get().views

        floralArrangementRepository.incrementViews(redRoseBouquet.id)
        entityManager.clear()

        val updated = floralArrangementRepository.findById(redRoseBouquet.id).get()
        assertEquals(initialViews + 1, updated.views)
    }

    @Test
    fun `incrementViews can be called multiple times consecutively`() {
        val arrangementId = summerSunflowers.id

        floralArrangementRepository.incrementViews(arrangementId)
        floralArrangementRepository.incrementViews(arrangementId)
        floralArrangementRepository.incrementViews(arrangementId)
        entityManager.clear()

        val updated = floralArrangementRepository.findById(arrangementId).get()
        assertEquals(3, updated.views)
    }

    // --- findByIdWithDetails ---

    @Test
    fun `findByIdWithDetails fetches arrangement with all details eagerly`() {
        val found = floralArrangementRepository.findByIdWithDetails(redRoseBouquet.id)

        assertNotNull(found)
        assertEquals(redRoseBouquet.id, found.id)
        assertEquals("Red Rose Bouquet", found.name)

        // Verify Price
        assertNotNull(found.price)
        assertTrue(BigDecimal("75000.00").compareTo(found.price.price) == 0)

        // Verify Description
        assertNotNull(found.description)
        assertEquals("A beautiful bouquet of premium red roses.", found.description?.description)

        // Verify Categories
        assertEquals(2, found.categories.size)
        val categoryIds = found.categories.map { it.id }.toSet()
        assertTrue(categoryIds.contains(romanticCategory.id))
        assertTrue(categoryIds.contains(birthdayCategory.id))

        // Verify Tags
        assertEquals(1, found.tags.size)
        assertEquals("Roses", found.tags.first().text)

        // Verify Flowers
        assertEquals(1, found.flowers.size)
        val flower = found.flowers.first()
        assertEquals("Red Rose", flower.name)
        assertEquals("Love and passion", flower.meaning)

        // Verify Gallery
        assertEquals(2, found.gallery.size)
        val primaryImage = found.gallery.firstOrNull { it.isPrimary }
        assertNotNull(primaryImage)
        assertEquals("img_rose_1", primaryImage.publicId)
    }

    @Test
    fun `findByIdWithDetails returns null for non-existent id`() {
        val found = floralArrangementRepository.findByIdWithDetails(999_999L)
        assertNull(found)
    }

    @Test
    fun `findByIdWithDetails works correctly when relations are empty or null`() {
        val found = floralArrangementRepository.findByIdWithDetails(summerSunflowers.id)

        assertNotNull(found)
        assertEquals(summerSunflowers.id, found.id)
        assertNull(found.description)
        assertTrue(found.categories.isEmpty())
        assertTrue(found.tags.isEmpty())
        assertTrue(found.flowers.isEmpty())
        assertTrue(found.gallery.isEmpty())
    }

    // --- id ---

    @Test
    fun `id returns list containing the floral arrangement with that id`() {
        val result = floralArrangementRepository.id(springTulips.id)

        assertEquals(1, result.size)
        assertEquals(springTulips.id, result.first().id)
        assertEquals("Spring Tulips", result.first().name)
    }

    @Test
    fun `id returns empty list when id does not exist`() {
        val result = floralArrangementRepository.id(999_999L)
        assertTrue(result.isEmpty())
    }

    // --- findAllWithFilters ---

    @Test
    fun `findAllWithFilters without any filters returns all arrangements paginated`() {
        val page = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"))
        )

        assertEquals(4, page.totalElements)
        assertEquals(4, page.content.size)
    }

    @Test
    fun `findAllWithFilters filters by categoryId correctly`() {
        val page = floralArrangementRepository.findAllWithFilters(
            categoryId = romanticCategory.id,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(2, page.totalElements)
        val ids = page.content.map { it.id }.toSet()
        assertEquals(setOf(redRoseBouquet.id, springTulips.id), ids)
    }

    @Test
    fun `findAllWithFilters filters by tagId correctly`() {
        val page = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = tulipTag.id,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(1, page.totalElements)
        assertEquals(springTulips.id, page.content.first().id)
    }

    @Test
    fun `findAllWithFilters filters by featured flag correctly`() {
        val featuredPage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = true,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(2, featuredPage.totalElements)
        val featuredIds = featuredPage.content.map { it.id }.toSet()
        assertEquals(setOf(redRoseBouquet.id, luxuryOrchids.id), featuredIds)

        val nonFeaturedPage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = false,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(2, nonFeaturedPage.totalElements)
        val nonFeaturedIds = nonFeaturedPage.content.map { it.id }.toSet()
        assertEquals(setOf(springTulips.id, summerSunflowers.id), nonFeaturedIds)
    }

    @Test
    fun `findAllWithFilters filters by seasonal flag correctly`() {
        val seasonalPage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = true,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(2, seasonalPage.totalElements)
        val seasonalIds = seasonalPage.content.map { it.id }.toSet()
        assertEquals(setOf(springTulips.id, summerSunflowers.id), seasonalIds)
    }

    @Test
    fun `findAllWithFilters filters by isAvailable flag correctly`() {
        val availablePage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = true,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(3, availablePage.totalElements)
        val availableIds = availablePage.content.map { it.id }.toSet()
        assertEquals(setOf(redRoseBouquet.id, springTulips.id, summerSunflowers.id), availableIds)

        val unavailablePage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = false,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(1, unavailablePage.totalElements)
        assertEquals(luxuryOrchids.id, unavailablePage.content.first().id)
    }

    @Test
    fun `findAllWithFilters filters by minPrice and maxPrice correctly`() {
        // minPrice filter
        val minPricePage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = BigDecimal("70000.00"),
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )
        assertEquals(2, minPricePage.totalElements)
        assertEquals(setOf(redRoseBouquet.id, luxuryOrchids.id), minPricePage.content.map { it.id }.toSet())

        // maxPrice filter
        val maxPricePage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = BigDecimal("60000.00"),
            pageable = PageRequest.of(0, 10)
        )
        assertEquals(2, maxPricePage.totalElements)
        assertEquals(setOf(springTulips.id, summerSunflowers.id), maxPricePage.content.map { it.id }.toSet())

        // Range filter
        val rangePage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = BigDecimal("48000.00"),
            maxPrice = BigDecimal("80000.00"),
            pageable = PageRequest.of(0, 10)
        )
        assertEquals(2, rangePage.totalElements)
        assertEquals(setOf(springTulips.id, redRoseBouquet.id), rangePage.content.map { it.id }.toSet())
    }

    @Test
    fun `findAllWithFilters combines multiple filters together`() {
        val page = floralArrangementRepository.findAllWithFilters(
            categoryId = romanticCategory.id,
            tagId = roseTag.id,
            featured = true,
            seasonal = false,
            isAvailable = true,
            minPrice = BigDecimal("50000.00"),
            maxPrice = BigDecimal("100000.00"),
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(1, page.totalElements)
        assertEquals(redRoseBouquet.id, page.content.first().id)
    }

    @Test
    fun `findAllWithFilters returns empty page when no arrangement matches filters`() {
        val page = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = BigDecimal("999999.00"),
            maxPrice = null,
            pageable = PageRequest.of(0, 10)
        )

        assertEquals(0, page.totalElements)
        assertTrue(page.content.isEmpty())
    }

    @Test
    fun `findAllWithFilters supports pagination with custom page sizes`() {
        val firstPage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"))
        )

        assertEquals(4, firstPage.totalElements)
        assertEquals(2, firstPage.totalPages)
        assertEquals(2, firstPage.content.size)

        val secondPage = floralArrangementRepository.findAllWithFilters(
            categoryId = null,
            tagId = null,
            featured = null,
            seasonal = null,
            isAvailable = null,
            minPrice = null,
            maxPrice = null,
            pageable = PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id"))
        )

        assertEquals(2, secondPage.content.size)
        // Ensure pages do not overlap
        val firstPageIds = firstPage.content.map { it.id }
        val secondPageIds = secondPage.content.map { it.id }
        assertTrue(firstPageIds.intersect(secondPageIds.toSet()).isEmpty())
    }

    // --- findWithCategoriesByIds ---

    @Test
    fun `findWithCategoriesByIds fetches arrangements with categories for specified ids`() {
        val result = floralArrangementRepository.findWithCategoriesByIds(
            listOf(redRoseBouquet.id, springTulips.id)
        )

        assertEquals(2, result.size)
        val bouquetResult = result.first { it.id == redRoseBouquet.id }
        assertEquals(2, bouquetResult.categories.size)

        val tulipResult = result.first { it.id == springTulips.id }
        assertEquals(1, tulipResult.categories.size)
    }

    @Test
    fun `findWithCategoriesByIds returns empty list when no ids match`() {
        val result = floralArrangementRepository.findWithCategoriesByIds(listOf(999_999L))
        assertTrue(result.isEmpty())
    }

    // --- findWithTagsByIds ---

    @Test
    fun `findWithTagsByIds fetches arrangements with tags for specified ids`() {
        val result = floralArrangementRepository.findWithTagsByIds(
            listOf(redRoseBouquet.id, springTulips.id)
        )

        assertEquals(2, result.size)
        val bouquetResult = result.first { it.id == redRoseBouquet.id }
        assertEquals(1, bouquetResult.tags.size)
        assertEquals("Roses", bouquetResult.tags.first().text)

        val tulipResult = result.first { it.id == springTulips.id }
        assertEquals(1, tulipResult.tags.size)
        assertEquals("Tulips", tulipResult.tags.first().text)
    }

    @Test
    fun `findWithTagsByIds returns empty list when no ids match`() {
        val result = floralArrangementRepository.findWithTagsByIds(listOf(999_999L))
        assertTrue(result.isEmpty())
    }

    // --- findPrimaryImagesByArrangementIds ---

    @Test
    fun `findPrimaryImagesByArrangementIds returns only primary images for given arrangements`() {
        val images = floralArrangementRepository.findPrimaryImagesByArrangementIds(
            listOf(redRoseBouquet.id, springTulips.id, luxuryOrchids.id)
        )

        assertEquals(3, images.size)
        assertTrue(images.all { it.isPrimary })

        val publicIds = images.map { it.publicId }.toSet()
        assertEquals(setOf("img_rose_1", "img_tulip_1", "img_orchid_1"), publicIds)
    }

    @Test
    fun `findPrimaryImagesByArrangementIds returns empty list when arrangement has no primary image or unknown ids`() {
        val images = floralArrangementRepository.findPrimaryImagesByArrangementIds(
            listOf(summerSunflowers.id, 999_999L)
        )
        assertTrue(images.isEmpty())
    }

    // --- countByIsAvailable ---

    @Test
    fun `countByIsAvailable returns correct count for available and unavailable arrangements`() {
        assertEquals(3, floralArrangementRepository.countByIsAvailable(true))
        assertEquals(1, floralArrangementRepository.countByIsAvailable(false))
    }

    // --- Constraints ---

    @Test
    fun `slug must be unique and throws DataIntegrityViolationException on duplicate`() {
        val duplicate = FloralArrangement(
            name = "Another Red Rose Bouquet",
            seoName = "another-red-rose-bouquet",
            slug = "red-rose-bouquet",
            price = Price(price = BigDecimal("60000.00"), currency = "COP"),
            isAvailable = true,
            seasonal = false,
            featured = false
        )

        assertFailsWith<DataIntegrityViolationException> {
            floralArrangementRepository.saveAndFlush(duplicate)
        }
    }

    // --- Helpers ---

    private fun saveCategory(name: String, slug: String): Category {
        val saved = categoryRepository.saveAndFlush(
            Category(
                name = name,
                slug = slug,
                path = "",
                depth = 0
            )
        )
        return categoryRepository.saveAndFlush(
            saved.copy(path = Category.buildRootPath(saved.id!!))
        )
    }

    private fun saveTag(text: String, route: String): Tag {
        return tagRepository.saveAndFlush(
            Tag(
                text = text,
                route = route,
                description = "Description for $text"
            )
        )
    }

    private fun saveArrangement(
        name: String,
        seoName: String,
        slug: String,
        price: BigDecimal,
        currency: String = "COP",
        discountPrice: BigDecimal? = null,
        isAvailable: Boolean = true,
        seasonal: Boolean = false,
        featured: Boolean = false,
        views: Int = 0,
        categories: Set<Category> = emptySet(),
        tags: Set<Tag> = emptySet(),
        descriptionText: String? = null,
        flowersList: List<Pair<String, String>> = emptyList(),
        images: List<Pair<String, Boolean>> = emptyList()
    ): FloralArrangement {
        val description = descriptionText?.let {
            ProductDescription(
                shortDescription = "Short description for $name",
                description = it
            )
        }

        val arrangement = FloralArrangement(
            name = name,
            seoName = seoName,
            slug = slug,
            price = Price(price = price, discountPrice = discountPrice, currency = currency),
            isAvailable = isAvailable,
            seasonal = seasonal,
            featured = featured,
            views = views,
            description = description,
            categories = categories.toMutableSet(),
            tags = tags.toMutableSet()
        )

        flowersList.forEach { (flName, flMeaning) ->
            val flower = Flowers(
                name = flName,
                meaning = flMeaning,
                floralArrangement = arrangement
            )
            arrangement.flowers.add(flower)
        }

        images.forEachIndexed { index, (publicId, isPrimary) ->
            val galleryItem = ProductGallery(
                publicId = publicId,
                originalUrl = "https://images.com/$publicId.jpg",
                thumbnailUrl = "https://images.com/thumb_$publicId.jpg",
                mediumUrl = "https://images.com/medium_$publicId.jpg",
                altText = "Alt text for $publicId",
                floralArrangement = arrangement,
                isPrimary = isPrimary,
                position = index
            )
            arrangement.gallery.add(galleryItem)
        }

        return floralArrangementRepository.saveAndFlush(arrangement)
    }
}
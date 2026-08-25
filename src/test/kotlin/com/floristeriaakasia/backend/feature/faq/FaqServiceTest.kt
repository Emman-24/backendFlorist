package com.floristeriaakasia.backend.feature.faq

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Sort
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class FaqServiceTest {

    @Mock
    private lateinit var faqRepository: FaqRepository

    @InjectMocks
    private lateinit var faqService: FaqService

    @BeforeEach
    fun setUp() {
        faqRepository = Mockito.mock(FaqRepository::class.java)
        faqService = FaqService(faqRepository)
    }

    @Test
    fun shouldFindAllActiveFaqs() {
        // Arrange
        val activeFaq = buildFaq(id = 1L, position = 0, status = true)
        val inactiveFaq = buildFaq(id = 2L, position = 1, status = false)
        val faqs = listOf(activeFaq, inactiveFaq)
        Mockito.`when`(faqRepository.findAll(Mockito.any<Sort>())).thenReturn(faqs)

        // Act
        val result = faqService.findAll(onlyActive = true)

        // Assert
        assertEquals(1, result.size)
        assertEquals(activeFaq.id, result.first().id)
        assertTrue(result.first().status)
    }

    @Test
    fun shouldFindAllNonActiveFaqs() {
        // Arrange
        val activeFaq = buildFaq(id = 1L, position = 0, status = true)
        val inactiveFaq = buildFaq(id = 2L, position = 1, status = false)
        val faqs = listOf(activeFaq, inactiveFaq)
        Mockito.`when`(faqRepository.findAll(Mockito.any<Sort>())).thenReturn(faqs)

        // Act
        val result = faqService.findAll(onlyActive = false)

        // Assert
        assertEquals(2, result.size)
        val ids = result.map { it.id }
        assertTrue(ids.contains(activeFaq.id))
        assertTrue(ids.contains(inactiveFaq.id))
    }

    @Test
    fun findAll() {
        // Arrange
        val activeFaq = buildFaq(id = 1L, position = 0, status = true)
        val inactiveFaq = buildFaq(id = 2L, position = 1, status = false)
        val faqs = listOf(activeFaq, inactiveFaq)
        Mockito.`when`(faqRepository.findAll(Mockito.any<Sort>())).thenReturn(faqs)

        // Act
        val result = faqService.findAll() // default onlyActive = false

        // Assert
        assertEquals(2, result.size)
    }

    @Test
    fun findById() {
        // Arrange
        val faq = buildFaq(id = 1L)
        Mockito.`when`(faqRepository.findById(1L)).thenReturn(java.util.Optional.of(faq))

        // Act
        val result = faqService.findById(1L)

        // Assert
        assertNotNull(result)
        assertEquals(faq.id, result.id)
        assertEquals(faq.question, result.question)
        assertEquals(faq.answer, result.answer)
        assertEquals(faq.position, result.position)
        assertEquals(faq.views, result.views)
        assertEquals(faq.status, result.status)
    }

    @Test
    fun findByIdAndIncrementViews() {
        // Arrange
        val originalViews = 5
        val faq = buildFaq(id = 1L, views = originalViews)
        Mockito.`when`(faqRepository.findById(1L)).thenReturn(java.util.Optional.of(faq))
        Mockito.`when`(faqRepository.save(Mockito.any())).thenAnswer {
            it.getArgument(0) as Faq
        }

        // Act
        val result = faqService.findByIdAndIncrementViews(1L)

        // Assert
        assertNotNull(result)
        assertEquals(faq.id, result.id)
        // The service increments the views by 1, so we expect originalViews + 1
        assertEquals(originalViews + 1, result.views)
    }

    @Test
    fun create() {
        // Arrange
        val request = CreateFaqRequest(
            question = "New question?",
            answer = "New answer.",
            position = 5,
            status = true
        )
        Mockito.`when`(faqRepository.save(Mockito.any())).thenAnswer {
            val faq = it.getArgument(0) as Faq
            faq.copy(id = 1L)
        }

        // Act
        val result = faqService.create(request)

        // Assert
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals(request.question.trim(), result.question)
        assertEquals(request.answer.trim(), result.answer)
        assertEquals(request.position, result.position)
        assertEquals(request.status, result.status)
    }

    @Test
    fun update() {
        // Arrange
        val id = 1L
        val request = UpdateFaqRequest(
            question = "New question?",
            answer = "New answer.",
            position = 10,
            status = true
        )
        Mockito.`when`(faqRepository.findById(id)).thenReturn(java.util.Optional.of(
            buildFaq(id = id, question = "Old question", answer = "Old answer", position = 0, status = false)
        ))
        Mockito.`when`(faqRepository.save(Mockito.any())).thenAnswer {
            val faq = it.getArgument(0) as Faq
            faq.question = request.question.trim()
            faq.answer = request.answer.trim()
            faq.position = request.position
            faq.status = request.status
            faq
        }

        // Act
        val result = faqService.update(id, request)

        // Assert
        assertNotNull(result)
        assertEquals(request.question.trim(), result.question)
        assertEquals(request.answer.trim(), result.answer)
        assertEquals(request.position, result.position)
        assertEquals(request.status, result.status)
    }

    @Test
    fun updateStatus() {
        // Arrange
        val id = 1L
        val existingFaq = buildFaq(id = id, status = false)
        val newStatus = true
        Mockito.`when`(faqRepository.findById(id)).thenReturn(java.util.Optional.of(existingFaq))
        Mockito.`when`(faqRepository.save(Mockito.any())).thenAnswer {
            val faq = it.getArgument(0) as Faq
            faq.status = newStatus
            faq
        }

        // Act
        val result = faqService.updateStatus(id, newStatus)

        // Assert
        assertNotNull(result)
        assertEquals(newStatus, result.status)
    }

    @Test
    fun delete() {
        // Arrange
        val id = 1L
        Mockito.`when`(faqRepository.existsById(id)).thenReturn(true)

        // Act
        faqService.delete(id)

        // Assert
        Mockito.verify(faqRepository).deleteById(id)
    }

    @Test
    fun reorder() {
        // Arrange
        val positions = mapOf(1L to 0, 2L to 1)
        val faq1 = buildFaq(id = 1L, position = 5)   // initial position 5
        val faq2 = buildFaq(id = 2L, position = 10)  // initial position 10
        val faqs = listOf(faq1, faq2)
        Mockito.`when`(faqRepository.findAllById(Mockito.any<Set<Long>>())).thenReturn(faqs)
        Mockito.`when`(faqRepository.saveAll(Mockito.any<List<Faq>>())).thenAnswer { it.getArgument(0) }

        // Act
        faqService.reorder(positions)

        // Assert
        assertEquals(0, faq1.position)
        assertEquals(1, faq2.position)
        Mockito.verify(faqRepository).saveAll(faqs)
    }

    private fun buildFaq(
        id: Long = 1L,
        question: String = "Sample question?",
        answer: String = "Sample answer.",
        position: Int = 0,
        views: Int = 0,
        status: Boolean = true
    ) = Faq(
        id = id,
        question = question,
        answer = answer,
        position = position,
        views = views,
        status = status
    )

}
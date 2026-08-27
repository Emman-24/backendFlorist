package com.floristeriaakasia.backend.feature.faq

import com.floristeriaakasia.backend.config.RepositoryTestBase
import com.floristeriaakasia.backend.feature.faq.infrastructure.api.Faq
import com.floristeriaakasia.backend.feature.faq.infrastructure.api.FaqRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FaqRepositoryTest : RepositoryTestBase() {

    @Autowired
    private lateinit var faqRepository: FaqRepository

    private lateinit var faq: Faq
    private lateinit var faq2: Faq
    private lateinit var faq3: Faq

    @BeforeEach
    fun setUp() {
        faq = Faq(question = "What is the best way to buy flowers?", answer = "Visit our store!")
        faq2 = Faq(question = "How do I contact customer support?", answer = "Call us")
        faq3 = Faq(question = "Where can i contact the store?", answer = "Using the contact form")
        faq3.status = false
        faqRepository.saveAll(listOf(faq, faq2, faq3))
    }

    @Test
    fun `findByQuestion returns the matching faq`() {
        assertEquals(faq, faqRepository.findByQuestion("What is the best way to buy flowers?"))
        assertEquals(faq2, faqRepository.findByQuestion("How do I contact customer support?"))
    }

    @Test
    fun `findByQuestion returns null when no faq has that question`() {
        assertNull(faqRepository.findByQuestion("Does not exist"))
        assertNull(faqRepository.findByQuestion(""))
    }

    @Test
    fun `countByStatus returns the number of faqs with the given status true`() {
        assertEquals(2, faqRepository.countByStatus(true))
    }

    @Test
    fun `countByStatus returns the number of faqs with the given status false`() {
        assertEquals(1, faqRepository.countByStatus(false))
    }

    @Test
    fun `findAllByStatusTrue returns only active faqs`() {
        assertEquals(faqRepository.findByStatus(true), listOf(faq, faq2))
    }

    @Test
    fun `findAllByStatusFalse returns only inactive faqs`() {
        assertEquals(faqRepository.findByStatus(false), listOf(faq3))
    }


}
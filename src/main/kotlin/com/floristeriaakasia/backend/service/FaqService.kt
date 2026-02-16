package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Faq
import com.floristeriaakasia.backend.repository.FaqRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FaqService(
    private val faqRepository: FaqRepository
) {
    fun getAllActiveFaqs(): List<Faq> {
        return faqRepository.findByStatusOrderByPositionAsc(true)
    }

    fun getFaqsByCategory(category: String): List<Faq> {
        return faqRepository.findByCategoryAndStatusOrderByPositionAsc(category, true)
    }

    fun searchFaqs(query: String): List<Faq> {
        return faqRepository.searchFaqs(query)
    }

    fun incrementViews(faqId: Long) {
        faqRepository.findById(faqId).ifPresent { faq ->
            faq.views++
            faqRepository.save(faq)
        }
    }

    fun incrementHelpful(faqId: Long) {
        faqRepository.findById(faqId).ifPresent { faq ->
            faq.helpfulCount++
            faqRepository.save(faq)
        }
    }

    @Transactional
    fun update(id: Long, faq: Faq): Faq {
        val existing = faqRepository.findByIdOrNull(id) ?: throw ResourceNotFoundException("Faq with id $id not found")

        existing.question = faq.question
        existing.answer = faq.answer
        existing.category = faq.category
        existing.position = faq.position
        existing.status = faq.status

        return faqRepository.save(existing)
    }

    @Transactional
    fun save(faq: Faq): Faq {
        return faqRepository.save(faq)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Faq? {
        return faqRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun search(query: String): List<Faq> {
        return faqRepository.searchFaqs(query)
    }

    @Transactional(readOnly = true)
    fun searchWithFilters(search: String?, category: String?, status: Boolean? = null): List<Faq> {
        return faqRepository.searchFaqsWithFilters(
            if (search.isNullOrBlank()) null else search,
            if (category.isNullOrBlank()) null else category,
            status
        )
    }

    @Transactional(readOnly = true)
    fun findByCategory(category: String): List<Faq> {
        return faqRepository.findByCategoryAndStatusOrderByPositionAsc(category, true)
    }

    @Transactional(readOnly = true)
    fun findAllIncludingInactive(): List<Faq> {
        return faqRepository.findAll().sortedBy { it.position }
    }

    @Transactional
    fun deleteById(id: Long) {
        val faq = faqRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("FAQ with id $id not found")

        faqRepository.delete(faq)
    }

    @Transactional
    fun toggleStatus(id: Long): Faq {
        val faq = faqRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("FAQ with id $id not found")

        faq.status = !faq.status
        return faqRepository.save(faq)
    }


}
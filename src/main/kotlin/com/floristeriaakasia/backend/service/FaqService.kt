package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Faq
import com.floristeriaakasia.backend.repository.FaqRepository
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
        return faqRepository.searchFaqs(query, 1)
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

}
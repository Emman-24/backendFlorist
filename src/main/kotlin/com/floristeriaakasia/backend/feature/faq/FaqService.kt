package com.floristeriaakasia.backend.feature.faq

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FaqService(
    private val faqRepository: FaqRepository
) {

    @Transactional
    fun save(faq: Faq): Faq {
        return faqRepository.save(faq)
    }

}
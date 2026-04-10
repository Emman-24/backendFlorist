package com.floristeriaakasia.backend.feature.faq

import com.floristeriaakasia.backend.global.exeption.FaqNotFoundException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FaqService(
    private val faqRepository: FaqRepository
) {
    @Transactional(readOnly = true)
    fun findAll(onlyActive: Boolean = false): List<FaqResponse> {
        val sort = Sort.by(Sort.Direction.ASC, "position")
        val faqs = faqRepository.findAll(sort)
        return faqs
            .let { if (onlyActive) it.filter { f -> f.status } else it }
            .map(FaqResponse::from)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): FaqResponse {
        val faq = faqRepository.findByIdOrNull(id) ?: throw FaqNotFoundException(id)
        return FaqResponse.from(faq)
    }

    fun findByIdAndIncrementViews(id: Long): FaqResponse {
        val faq = faqRepository.findByIdOrNull(id) ?: throw FaqNotFoundException(id)
        faq.views++
        return FaqResponse.from(faqRepository.save(faq))
    }

    fun create(request: CreateFaqRequest): FaqResponse {
        val faq = Faq(
            question = request.question.trim(),
            answer = request.answer.trim(),
            position = request.position,
            status = request.status
        )
        return FaqResponse.from(faqRepository.save(faq))
    }

    fun update(id: Long, request: UpdateFaqRequest): FaqResponse {
        val faq = faqRepository.findByIdOrNull(id) ?: throw FaqNotFoundException(id)
        faq.question = request.question.trim()
        faq.answer = request.answer.trim()
        faq.position = request.position
        faq.status = request.status
        return FaqResponse.from(faqRepository.save(faq))
    }

    fun updateStatus(id: Long, status: Boolean): FaqResponse {
        val faq = faqRepository.findByIdOrNull(id) ?: throw FaqNotFoundException(id)
        faq.status = status
        return FaqResponse.from(faqRepository.save(faq))
    }

    fun delete(id: Long) {
        if (!faqRepository.existsById(id)) throw FaqNotFoundException(id)
        faqRepository.deleteById(id)
    }

    fun reorder(positions: Map<Long, Int>) {
        val faqs = faqRepository.findAllById(positions.keys)
        faqs.forEach { faq ->
            positions[faq.id]?.let { faq.position = it }
        }
        faqRepository.saveAll(faqs)
    }

}
package com.floristeriaakasia.backend.feature.faq.infrastructure.api

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FaqRepository: JpaRepository<Faq, Long>{
    fun countByStatus(status: Boolean): Long
    fun findByQuestion(question: String): Faq?
    fun findByStatus(status: Boolean): List<Faq>
}
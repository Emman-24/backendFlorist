package com.floristeriaakasia.backend.feature.faq

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FaqRepository: JpaRepository<Faq, Long>
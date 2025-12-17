package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository: JpaRepository<Tag, Long> {
    fun findByStatus(status: Boolean): List<Tag>
}
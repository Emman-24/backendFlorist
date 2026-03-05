package com.floristeriaakasia.backend.feature.product.application.port.out

import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain

interface ProductRepositoryPort {
    fun findById(id: Long): FloralArrangementDomain?
    fun save(domain: FloralArrangementDomain): FloralArrangementDomain
}
package com.floristeriaakasia.backend.feature.product.application.port.out

import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain

interface SaveProductPort {
    fun save(domain: FloralArrangementDomain): FloralArrangementDomain
}
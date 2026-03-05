package com.floristeriaakasia.backend.feature.product.adapter.out.persistence

import com.floristeriaakasia.backend.feature.product.application.port.out.ProductRepositoryPort
import com.floristeriaakasia.backend.feature.product.domain.model.FloralArrangementDomain
import org.springframework.stereotype.Component

@Component
class ProductPersistenceAdapter(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) : ProductRepositoryPort {
    override fun findById(id: Long): FloralArrangementDomain? {
        val entity = productRepository.findById(id).orElse(null) ?: return null
        return productMapper.toDomain(entity)
    }

    override fun save(domain: FloralArrangementDomain): FloralArrangementDomain {
        val entityToSave = productMapper.toEntity(domain)
        val savedEntity = productRepository.save(entityToSave)
        return productMapper.toDomain(savedEntity)
    }

}
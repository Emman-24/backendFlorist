package com.floristeriaakasia.backend.feature.product.domain.exception

class ProductNotFoundException(id: Long) : RuntimeException("Floral arrangement with ID $id not found.")
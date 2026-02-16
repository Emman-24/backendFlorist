package com.floristeriaakasia.backend.exception

class InsufficientStockException(productId: Long, requested: Int, available: Int) :
    RuntimeException("Insufficient stock for product $productId. Requested: $requested, Available: $available")
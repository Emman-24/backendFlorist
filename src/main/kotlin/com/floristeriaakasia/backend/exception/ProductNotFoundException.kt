package com.floristeriaakasia.backend.exception

class ProductNotFoundException(id: Long) : RuntimeException("Product with id $id not found")
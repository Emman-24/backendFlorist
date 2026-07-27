package com.floristeriaakasia.backend.global.exeption

class FlowerNotFoundException(id: Long) : RuntimeException("Flower with ID $id not found.")
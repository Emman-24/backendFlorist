package com.floristeriaakasia.backend.global.exeption

class FaqNotFoundException(id: Long) : RuntimeException(
    "FAQ with ID $id not found."
)

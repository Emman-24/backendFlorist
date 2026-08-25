package com.floristeriaakasia.backend.global.exeption

class FaqNotFoundException(val id: Long) : RuntimeException(
    "FAQ with ID $id not found."
)

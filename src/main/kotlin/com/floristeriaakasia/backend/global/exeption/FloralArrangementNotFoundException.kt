package com.floristeriaakasia.backend.global.exeption

class FloralArrangementNotFoundException(id: Long) : RuntimeException(
    "Floral arrangement with ID $id not found."
)

class FloralArrangementSlugNotFoundException(slug: String) : RuntimeException(
    "Floral arrangement with slug $slug not found."
)
package com.floristeriaakasia.backend.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app.image-storage")
@Component
data class ImageStorageProperties(
    val basePath: String,
    val allowedMimeTypes: Set<String>
) {
    constructor() : this(
        "./images",
        setOf(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp",
            "image/avif"
        )
    )
}
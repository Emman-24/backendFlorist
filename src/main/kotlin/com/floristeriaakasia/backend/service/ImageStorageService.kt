package com.floristeriaakasia.backend.service

import org.springframework.web.multipart.MultipartFile

interface ImageStorageService {
    fun upload(file: MultipartFile, folder: String): ImageUrls
    fun delete(publicId: String)
}

data class ImageUrls(
    val publicId: String,
    val original: String,
    val thumbnail: String,
    val medium: String
)
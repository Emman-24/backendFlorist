package com.floristeriaakasia.backend.feature.floralArrangment.application

import org.springframework.web.multipart.MultipartFile

interface ImageStoragePort {
    fun uploadImage(file: MultipartFile, folder: String): UploadedImageDto
    fun deleteImage(publicId: String)
}

data class UploadedImageDto(
    val publicId: String,
    val original: String,
    val thumbnail: String,
    val medium: String
)

package com.floristeriaakasia.backend.feature.floralArrangment.application

import org.springframework.web.multipart.MultipartFile

interface AddImageToFloralArrangementUseCase {
    fun execute(arrangementId: Long, image: MultipartFile, altText: String)
}
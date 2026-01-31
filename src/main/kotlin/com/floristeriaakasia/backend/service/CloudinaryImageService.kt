package com.floristeriaakasia.backend.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.cloudinary.utils.ObjectUtils.emptyMap
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CloudinaryImageService(
    private val cloudinary: Cloudinary
) : ImageStorageService {

    override fun upload(
        file: MultipartFile,
        folder: String
    ): ImageUrls {
        val uploadParams = mapOf(
            "folder" to "floristeria-akasia/$folder",
            "transformation" to listOf(
                mapOf("quality" to "auto", "fetch_format" to "auto")
            ),
            "eager" to listOf(
                mapOf("width" to 200, "height" to 200, "crop" to "fill"),
                mapOf("width" to 800, "height" to 800, "crop" to "limit")
            )
        )

        val result = cloudinary.uploader().upload(file.bytes, uploadParams)

        val publicId = result["public_id"] as String
        val version = result["version"] as Int

        return ImageUrls(
            publicId = publicId,
            original = cloudinary.url()
                .version(version.toString())
                .generate(publicId),
            thumbnail = cloudinary.url()
                .transformation(Transformation<Transformation<*>>().width(200).height(200).crop("fill"))
                .version(version.toString())
                .generate(publicId),
            medium = cloudinary.url()
                .transformation(Transformation<Transformation<*>>().width(800).height(800).crop("limit"))
                .version(version.toString())
                .generate(publicId)
        )

    }

    override fun delete(publicId: String) {
        cloudinary.uploader().destroy(publicId,emptyMap())
    }
}
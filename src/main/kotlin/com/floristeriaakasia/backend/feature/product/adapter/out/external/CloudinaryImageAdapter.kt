package com.floristeriaakasia.backend.feature.product.adapter.out.external

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.floristeriaakasia.backend.feature.product.application.port.out.ImageStoragePort
import com.floristeriaakasia.backend.feature.product.application.port.out.UploadedImageDto
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class CloudinaryImageAdapter(
    private val cloudinary: Cloudinary
): ImageStoragePort {

    override fun uploadImage(
        file: MultipartFile,
        folder: String
    ): UploadedImageDto {
        try {
            val uploadParams = mapOf(
                "folder" to "floristeria-akasia/$folder",
                "transformation" to listOf(
                    mapOf("quality" to "auto", "fetch_format" to "auto")
                ),
                "eager" to listOf(
                    mapOf("width" to 200, "height" to 200, "crop" to "fill"),
                    mapOf("width" to 800, "height" to 800, "crop" to "limit")
                ),
                "resource_type" to "image",
                "type" to "upload"
            )

            val result = cloudinary.uploader().upload(file.bytes, uploadParams)

            val publicId = result["public_id"] as String
            val version = result["version"] as Int

            return UploadedImageDto(
                publicId = publicId,
                original = cloudinary.url().version(version).generate(publicId),
                thumbnail = cloudinary.url()
                    .transformation(Transformation<Transformation<*>>().width(200).height(200).crop("fill"))
                    .version(version).generate(publicId),
                medium = cloudinary.url()
                    .transformation(Transformation<Transformation<*>>().width(800).height(800).crop("limit"))
                    .version(version).generate(publicId)
            )
        } catch (e: Exception) {
            throw IllegalStateException("Cloudinary upload failed: ${e.message}", e)
        }
    }

    override fun deleteImage(publicId: String) {
        try {
            cloudinary.uploader().destroy(publicId, emptyMap<String, Any>())
        } catch (e: Exception) {
            throw IllegalStateException("Cloudinary deletion failed: ${e.message}", e)
        }
    }

}
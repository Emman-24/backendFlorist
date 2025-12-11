package com.floristeriaakasia.backend.service

import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.UUID
import java.text.Normalizer

@Service
class LocalImageStorageService(
    properties: ImageStorageProperties,
) {
    private val rootPath: Path = Paths.get(properties.basePath)

    fun storeFile(
        inputStream: InputStream,
        originalFileName: String,
        categoryName: String,
        subcategoryName: String
    ): String {

        /**
         * Example of rooth path :
         * ./category/subcategory/
         */
        val dateDirectory = rootPath.resolve(
            slugify(categoryName) +
                    File.separator +
                    slugify(subcategoryName)
        )
        Files.createDirectories(dateDirectory)

        val ext = getFileExtension(originalFileName)

        /**
         * Example of stored name :
         * 16823456789.jpg
         */
        val storedName = "${UUID.randomUUID()}${if (ext.isEmpty()) "" else ".$ext"}"

        val filePath = dateDirectory.resolve(storedName)

        Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return rootPath.relativize(filePath).toString()
    }


    fun getFileExtension(fileName: String): String {
        val lastDost = fileName.lastIndexOf('.')
        return if (lastDost == -1) "" else fileName.substring(lastDost + 1)
    }

    private fun slugify(input: String): String {
        // Normalize and remove diacritics
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        // Lowercase, replace non-alphanumeric with dashes, collapse multiple dashes, trim dashes
        return normalized
            .lowercase()
            .replace("[^a-z0-9]+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
            .trim('-')
    }

    fun deleteFile(storePath: String) {
        if (storePath.isBlank()) return
        val filePath: Path = rootPath.resolve(storePath).normalize().toAbsolutePath()
        val normalizeRoot: Path = rootPath.normalize().toAbsolutePath()

        if (!filePath.startsWith(normalizeRoot)) {
            throw SecurityException("Acess denied")
        }

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath)
            }
        } catch (_: IOException) {

        }
    }
}
package com.floristeriaakasia.backend.controller.api

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(
) {

//    @PostMapping("/upload")
//    fun uploadImage(
//        @RequestParam("file") file: MultipartFile
//    ): ResponseEntity<*> {
//        return try {
//            ResponseEntity.ok(imageService.create(file))
//        } catch (e: IOException) {
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
//        } catch (e: IllegalArgumentException) {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
//        }
//    }

}
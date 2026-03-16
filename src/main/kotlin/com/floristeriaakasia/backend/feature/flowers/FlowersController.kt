package com.floristeriaakasia.backend.feature.flowers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flowers")
class FlowersController(
    private val flowersService: FlowersService
) {
    @PostMapping
    fun createFlower(
        @Valid @RequestBody request : CreateFlowersDTO
    ): ResponseEntity<Void> {
        flowersService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
package com.floristeriaakasia.backend.feature.flowers

import com.floristeriaakasia.backend.util.ApiResponse
import com.floristeriaakasia.backend.util.toSuccessResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flowers")
class FlowersController(
    private val flowersService: FlowersService
) {
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun createFlower(
        @Valid @RequestBody request: CreateFlowersDTO
    ): ResponseEntity<Void> {
        flowersService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<FlowerResponse>> =
        ResponseEntity.ok(flowersService.findById(id).toSuccessResponse())

    @GetMapping("/arrangement/{arrangementId}")
    fun getByArrangement(
        @PathVariable arrangementId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<FlowerResponse>> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100))
        return ResponseEntity.ok(flowersService.findByArrangement(arrangementId, pageable))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateFlowersDTO
    ): ResponseEntity<ApiResponse<FlowerResponse>> =
        ResponseEntity.ok(flowersService.update(id, request).toSuccessResponse("Flower updated successfully"))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        flowersService.delete(id)
        return ResponseEntity.noContent().build()
    }

}
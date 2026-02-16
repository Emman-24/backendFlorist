package com.floristeriaakasia.backend.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequestDTO(
    @field:NotBlank(message = "El username es obligatorio")
    @field:Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    val username: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val password: String
)


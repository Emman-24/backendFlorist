package com.floristeriaakasia.backend.feature.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequestDTO(
    @field:NotBlank(message = "El username es obligatorio")
    @field:Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos"
    )
    val username: String,

    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "Email inválido")
    @field:Size(max = 100, message = "El email no puede exceder 100 caracteres")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    val password: String,

    @field:Size(max = 100, message = "El nombre completo no puede exceder 100 caracteres")
    val fullName: String? = null
)
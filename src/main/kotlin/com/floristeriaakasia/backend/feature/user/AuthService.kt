package com.floristeriaakasia.backend.feature.user

import com.floristeriaakasia.backend.feature.role.Role
import com.floristeriaakasia.backend.feature.role.RoleRepository
import com.floristeriaakasia.backend.global.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant


@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // ── Login ─────────────────────────────────────────────────────────────
    @Transactional
    fun login(request: LoginRequestDTO): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )

        val user = userRepository.findByUsername(request.username)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        log.info("AUTH_LOGIN_OK username={}", user.username)
        return buildAuthResponse(user)
    }

    @Transactional
    fun register(request: RegisterRequestDTO): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("El username ya existe")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val userRole = roleRepository.findByName(Role.USER).orElseGet {
            roleRepository.save(Role(name = Role.USER, description = "Usuario regular"))
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            enabled = true,
            roles = mutableSetOf(userRole)
        )

        val saved = userRepository.save(user)
        log.info("AUTH_REGISTER_OK username={}", saved.username)
        return buildAuthResponse(saved)
    }

    @Transactional(readOnly = true)
    fun refreshToken(refreshToken: String): AuthResponse {
        val username = jwtService.extractUsername(refreshToken)

        val user = userRepository.findByUsername(username)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw IllegalArgumentException("Refresh token inválido o expirado")
        }

        val newAccessToken = jwtService.generateToken(user)
        log.debug("TOKEN_REFRESHED username={}", username)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = jwtService.getExpirationTime(),
            user = UserInfo.from(user)
        )
    }


    private fun buildAuthResponse(user: User) = AuthResponse(
        accessToken = jwtService.generateToken(user),
        refreshToken = jwtService.generateRefreshToken(user),
        tokenType = "Bearer",
        expiresIn = jwtService.getExpirationTime(),
        user = UserInfo.from(user)
    )
}

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val roles: List<String>
) {
    companion object {
        fun from(user: User) = UserInfo(
            id = user.id!!,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            roles = user.roles.map { it.name }
        )
    }
}
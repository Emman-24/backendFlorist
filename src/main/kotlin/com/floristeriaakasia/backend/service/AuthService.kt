package com.floristeriaakasia.backend.service

import com.floristeriaakasia.backend.model.Role
import com.floristeriaakasia.backend.model.User
import com.floristeriaakasia.backend.repository.RoleRepository
import com.floristeriaakasia.backend.repository.UserRepository
import com.floristeriaakasia.backend.security.JwtService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    @Transactional
    fun login(
        username: String,
        password: String,
        response: HttpServletResponse
    ): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )

        val user = userRepository.findByUsername(username).orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        user.lastLoginAt = LocalDateTime.now()
        userRepository.save(user)

        val accessToken = jwtService.generateToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)

        response.let {
            val cookie = Cookie("accessToken", accessToken).apply {
                isHttpOnly = true
                secure = true
                path = "/"
                maxAge = 86400
            }
            it.addCookie(cookie)
        }

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 86400,
            user = UserInfo.from(user)
        )
    }


    @Transactional
    fun register(request: RegisterRequest): AuthResponse {

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("El username ya existe")
        }
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val userRole = roleRepository.findByName(Role.USER).orElseGet {
            roleRepository.save(
                Role(name = Role.USER, description = "Usuario regular")
            )
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            enabled = true,
            roles = mutableSetOf(userRole)
        )

        val savedUser = userRepository.save(user)

        val accessToken = jwtService.generateToken(savedUser)
        val refreshToken = jwtService.generateRefreshToken(savedUser)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 86400,
            user = UserInfo.from(savedUser)
        )
    }

    @Transactional(readOnly = true)
    fun refreshToken(refreshToken: String): AuthResponse {

        val username = jwtService.extractUsername(refreshToken)

        val user =
            userRepository.findByUsername(username).orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw IllegalArgumentException("Refresh token inválido o expirado")
        }

        val newAccessToken = jwtService.generateToken(user)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            tokenType = "Bearer",
            expiresIn = 86400,
            user = UserInfo.from(user)
        )
    }

    @Transactional
    fun createAdminUser(
        username: String,
        email: String,
        password: String
    ): User {

        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("El username ya existe")
        }

        val adminRole = roleRepository.findByName(Role.ADMIN).orElseGet {
            roleRepository.save(
                Role(name = Role.ADMIN, description = "Administrador del sistema")
            )
        }

        val admin = User(
            username = username,
            email = email,
            password = passwordEncoder.encode(password),
            fullName = "Administrador",
            enabled = true,
            roles = mutableSetOf(adminRole)
        )

        return userRepository.save(admin)
    }

    @Transactional
    fun createUserWithRole(
        username: String,
        email: String,
        password: String,
        fullName: String,
        roleName: String
    ): User {

        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("El username ya existe")
        }

        val roleDescription = when (roleName) {
            Role.ADMIN -> "Administrador del sistema"
            Role.MANAGER -> "Gerente"
            Role.USER -> "Usuario regular"
            else -> "Usuario"
        }

        val role = roleRepository.findByName(roleName).orElseGet {
            roleRepository.save(
                Role(name = roleName, description = roleDescription)
            )
        }

        val user = User(
            username = username,
            email = email,
            password = passwordEncoder.encode(password),
            fullName = fullName,
            enabled = true,
            roles = mutableSetOf(role)
        )

        return userRepository.save(user)
    }
}


data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String?
)


data class LoginRequest(
    val username: String,
    val password: String
)

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
        fun from(user: User): UserInfo {
            return UserInfo(
                id = user.id!!,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                roles = user.roles.map { it.name }
            )
        }
    }
}
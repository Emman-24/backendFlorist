package com.floristeriaakasia.backend.repository

import com.floristeriaakasia.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    fun findByUsername(username: String): Optional<User>

    fun findByEmail(email: String): Optional<User>

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    fun findByRoleName(roleName: String): List<User>

    fun findByEnabled(enabled: Boolean): List<User>

}
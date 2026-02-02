package com.floristeriaakasia.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users",
    indexes = [
        Index(name = "idx_username", columnList = "username", unique = true),
        Index(name = "idx_email", columnList = "email", unique = true)
    ]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    private val username: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false)
    private val password: String,

    @Column(length = 100)
    val fullName: String? = null,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var accountNonExpired: Boolean = true,

    @Column(nullable = false)
    var accountNonLocked: Boolean = true,

    @Column(nullable = false)
    var credentialsNonExpired: Boolean = true,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var lastLoginAt: LocalDateTime? = null
): UserDetails {

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return roles.map { role -> SimpleGrantedAuthority("ROLE_${role.name}") }
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = accountNonExpired

    override fun isAccountNonLocked(): Boolean = accountNonLocked

    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired

    override fun isEnabled(): Boolean = enabled

    fun hasRole(roleName: String): Boolean {
        return roles.any { it.name == roleName }
    }

}
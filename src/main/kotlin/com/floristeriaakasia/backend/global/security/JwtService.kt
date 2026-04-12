package com.floristeriaakasia.backend.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


@Service
class JwtService(

    @Value("\${security.jwt.secret-key}")
    private val secretKey: String,

    @Value("\${security.jwt.expiration-time}")
    private val jwtExpiration: Long,

    @Value("\${security.refresh-expiration-ms}")
    private val refreshExpiration: Long
) {

    private val signingKey: SecretKey = buildKey(secretKey)


    fun generateToken(userDetails: UserDetails): String {
        val claims: Map<String, Any> = mapOf(
            "roles" to userDetails.authorities.map { it.authority },
            "typ" to "access"
        )
        return buildToken(claims, userDetails.username, jwtExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(mapOf("typ" to "refresh"), userDetails.username, refreshExpiration)
    }


    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val claims = safeExtract(token) ?: return false
        val username = claims.subject ?: return false
        val typ = claims["typ"] as? String ?: return false
        if (typ != "access") return false

        return username == userDetails.username && !isExpired(claims)
    }

    fun isRefreshTokenValid(token: String, userDetails: UserDetails): Boolean {
        val claims = safeExtract(token) ?: return false
        val username = claims.subject ?: return false
        val typ = claims["typ"] as? String ?: return false
        if (typ != "refresh") return false
        return username == userDetails.username && !isExpired(claims)
    }


    fun extractUsername(token: String): String = extractAllClaims(token).subject

    fun extractRoles(token: String): List<String> =
        safeExtract(token)?.let {
            @Suppress("UNCHECKED_CAST")
            it["roles"] as? List<String>
        } ?: emptyList()

    fun getExpirationTime(): Long = jwtExpiration

    fun getRemainingTtl(token: String): Long {
        val claims = safeExtract(token) ?: return 0L
        val expMs = claims.expiration?.time ?: return 0L
        return (expMs - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    private fun buildToken(claims: Map<String, Any>, subject: String, ttlMs: Long): String =
        Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + ttlMs))
            .signWith(signingKey)
            .compact()

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .clockSkewSeconds(30)
            .build()
            .parseSignedClaims(token)
            .payload

    private fun safeExtract(token: String): Claims? =
        try { extractAllClaims(token) } catch (_: Exception) { null }

    private fun isExpired(claims: Claims): Boolean =
        claims.expiration?.before(Date()) ?: true


    private fun buildKey(raw: String): SecretKey {
        val bytes = raw.toByteArray(Charsets.UTF_8)
        require(bytes.size >= 32) {
            "security.jwt.secret-key must be at least 32 bytes (256 bits). " +
                    "Current length: ${bytes.size} bytes. " +
                    "Generate with: openssl rand -hex 32"
        }
        return Keys.hmacShaKeyFor(bytes)
    }

}
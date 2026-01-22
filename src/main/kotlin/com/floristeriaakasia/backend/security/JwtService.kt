package com.floristeriaakasia.backend.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
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

    private val signingKey: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    fun generateToken(userDetails: UserDetails): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["roles"] = userDetails.authorities.map { it.authority }
        return buildToken(claims, userDetails.username, jwtExpiration)
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(hashMapOf(), userDetails.username, refreshExpiration)
    }


    fun extractUsername(token: String): String {
        return extractClaim(token) { obj: Claims? -> obj!!.subject }
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }


    fun getExpirationTime(): Long {
        return jwtExpiration
    }

    private fun buildToken(
        claims: Map<String, Any>,
        username: String,
        expiration: Long
    ): String {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username: String = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token) { obj: Claims? -> obj!!.expiration }
    }


    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
    }

    fun extractRoles(token: String): List<String> {
        return try {
            val claims = extractAllClaims(token)
            claims["roles"] as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


}
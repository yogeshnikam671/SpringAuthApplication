package com.yogesh.authapplication.utils

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.apache.logging.log4j.LogManager
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenManager {

    private val logger = LogManager.getLogger(TokenManager::class)

    fun generateJwtToken(userDetails: UserDetails): String {
        val currentTime = Date(System.currentTimeMillis())
        val expirationTime = Date(System.currentTimeMillis() + TOKEN_VALIDITY)

        val key = Keys.hmacShaKeyFor(HMAC_COMPATIBLE_SECRET.toByteArray())

        return Jwts.builder()
            .setClaims(emptyMap<String, String>())
            .setSubject(userDetails.username)
            .setIssuedAt(currentTime)
            .setExpiration(expirationTime)
            .signWith(key)
            .compact()
    }

    fun validateJwtToken(userDetails: UserDetails, token: String): Boolean {
        val jwtParser = Jwts.parserBuilder().setSigningKey(HMAC_COMPATIBLE_SECRET.toByteArray()).build()
        return try {
            val claims = jwtParser.parseClaimsJws(token).body

            val isTokenExpired = claims.expiration.before(Date())
            val username = claims.subject
            val isUsernameValid = username.equals(userDetails.username)

            isUsernameValid && !isTokenExpired
        } catch (e: JwtException) {
            logger.info(e.message)
            false
        }
    }

    companion object {
        private const val TOKEN_VALIDITY = 120 * 1000 // 120 seconds
        private const val HMAC_COMPATIBLE_SECRET = "yogeshnikam@1029384756#1234567890^nikam"
    }
}

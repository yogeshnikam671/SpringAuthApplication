package com.yogesh.authapplication.utils

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User

class TokenManagerTest {

    // mocked data
    private val username = "username"
    private val hashedPassword = "hashedPassword"
    private val userDetails = User.withUsername(username)
        .password(hashedPassword)
        .roles("USER")
        .build()

    // mocked dependencies

    private val tokenManager = TokenManager()

    @Test
    fun `should generate jwt token`() {
        val token = tokenManager.generateJwtToken(userDetails)
        println(token)
    }

    @Test
    fun `should validate jwt token`() {
        val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlhdCI6MTY1ODU3NTg2MSwiZXhwIjoxNjU4NTc1OTIxfQ.fvPpMc1VX5KigLfOERWuF7DEcyc4bE2DaRAYzVR5xbc"
        val isValid = tokenManager.validateJwtToken(userDetails, token)
        isValid shouldBe false
    }
}

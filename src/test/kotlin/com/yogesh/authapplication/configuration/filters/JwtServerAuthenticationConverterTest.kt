package com.yogesh.authapplication.configuration.filters

import com.yogesh.authapplication.service.UserAuthDetailsService
import com.yogesh.authapplication.utils.TokenManager
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.server.ServerWebExchange
import reactor.kotlin.core.publisher.toMono

class JwtServerAuthenticationConverterTest {
    // mocked data
    private val username = "username"
    private val hashedPassword = "hashedPassword"
    private val userRole = "USER"
    private val userDetails = User.withUsername(username).password(hashedPassword).roles(userRole).build()
    private val token = "someToken"

    // mocked dependencies
    private val tokenManager = mockk<TokenManager> {
        every { extractUsername(any()) } returns username
        every { validateJwtToken(any(), any()) } returns true
    }

    private val userAuthDetailsService = mockk<UserAuthDetailsService> {
        every { findByUsername(username) } returns userDetails.toMono()
    }

    private fun getExchange(httpHeaders: HttpHeaders): ServerWebExchange {
        val httpRequest = mockk<ServerHttpRequest> {
            every { headers } returns httpHeaders
        }
        val exchange = mockk<ServerWebExchange> {
            every { request } returns httpRequest
        }
        return exchange
    }

    private val jwtServerAuthenticationConverter = JwtServerAuthenticationConverter(
        tokenManager,
        userAuthDetailsService
    )

    @Test
    fun `should return empty mono if the Authorization header is missing from the request`() {
        val httpHeaders = HttpHeaders(
            MultiValueMapAdapter(mutableMapOf("random" to mutableListOf("random")))
        )
        val exchange = getExchange(httpHeaders)

        val result = jwtServerAuthenticationConverter.convert(exchange).block()

        result shouldBe null
    }

    @Test
    fun `should return empty mono if the Authorization header does not start with Bearer prefix`() {
        val httpHeaders = HttpHeaders(
            MultiValueMapAdapter(mutableMapOf("Authorization" to mutableListOf("random")))
        )
        val exchange = getExchange(httpHeaders)

        val result = jwtServerAuthenticationConverter.convert(exchange).block()

        result shouldBe null
    }

    @Test
    fun `should validate the jwt token and return Authentication object if the bearer authorization token is present in the header`() {
        val httpHeaders = HttpHeaders(
            MultiValueMapAdapter(mutableMapOf("Authorization" to mutableListOf("Bearer $token")))
        )
        val exchange = getExchange(httpHeaders)

        val result = jwtServerAuthenticationConverter.convert(exchange).block()

        verify(exactly = 1) {
            tokenManager.extractUsername(token)
            userAuthDetailsService.findByUsername(username)
            tokenManager.validateJwtToken(userDetails, token)
        }

        result.shouldBeInstanceOf<Authentication>()
        result!!.credentials shouldBe hashedPassword
        result.principal shouldBe username
    }
}

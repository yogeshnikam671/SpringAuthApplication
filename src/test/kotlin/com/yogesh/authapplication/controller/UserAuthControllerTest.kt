package com.yogesh.authapplication.controller

import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.model.response.AuthenticationResponse
import com.yogesh.authapplication.service.UserAuthService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class UserAuthControllerTest {

    // mocked data
    private val username = "username"
    private val password = "password"
    private val jwtToken = "jwtToken"
    private val plainUser = User(username, password)
    private val authenticationResponse = AuthenticationResponse(
        token = jwtToken,
        isAuthenticated = true
    )

    // mocked dependencies
    private val userAuthService = mockk<UserAuthService> {
        every { register(any()) } returns Mono.just(true)
        every { authenticate(any()) } returns Mono.just(true)
        every { authenticateUsingAuthenticationManager(any()) } returns Mono.just(authenticationResponse)
    }

    private val userAuthController = UserAuthController(
        userAuthService = userAuthService
    )

    @Test
    fun `should register user`() {
        userAuthController.registerUser(plainUser).block()

        verify(exactly = 1) {
            userAuthService.register(plainUser)
        }
    }

    @Test
    fun `should authenticate user`() {
        userAuthController.authenticateUser(plainUser).block()

        verify(exactly = 1) {
            userAuthService.authenticate(plainUser)
        }
    }

    @Test
    fun `should authenticate user using authentication manager`() {
        userAuthController.authenticateUserV2(plainUser).block()

        verify(exactly = 1) {
            userAuthService.authenticateUsingAuthenticationManager(plainUser)
        }
    }
}

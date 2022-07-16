package com.yogesh.authapplication.controller

import com.yogesh.authapplication.model.UserAuthData
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
    private val plainUserAuthData = UserAuthData(username, password)

    // mocked dependencies
    private val userAuthService = mockk<UserAuthService> {
        every { register(any(), any()) } returns Mono.just(true)
        every { authenticate(any()) } returns Mono.just(true)
    }

    private val userAuthController = UserAuthController(
        userAuthService = userAuthService
    )

    @Test
    fun `should register user`() {
        userAuthController.registerUser(plainUserAuthData).block()

        verify(exactly = 1) {
            userAuthService.register(username, password)
        }
    }

    @Test
    fun `should authenticate user`() {
        userAuthController.authenticateUser(plainUserAuthData).block()

        verify(exactly = 1) {
            userAuthService.authenticate(plainUserAuthData)
        }
    }
}

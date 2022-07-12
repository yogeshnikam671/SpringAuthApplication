package com.yogesh.authapplication.service

import com.yogesh.authapplication.model.UserAuthData
import com.yogesh.authapplication.repository.UserAuthRepository
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono

class UserAuthServiceTest {

    // mocked data
    private val username = "username"
    private val password = "password"
    private val hashedPassword = "hashedPassword"
    private val userAuthData = UserAuthData(username, hashedPassword)

    // mocked dependencies
    private val bcryptPasswordEncoder = mockk<BCryptPasswordEncoder> {
        every { encode(any()) } returns hashedPassword
    }
    private val userAuthRepository = mockk<UserAuthRepository>(relaxed = true)

    // tests
    private val userAuthService = UserAuthService(
        bcryptPasswordEncoder,
        userAuthRepository
    )

    @Nested
    inner class RegisterUserTests {
        @Test
        fun `should hash the plain text password using bcrypt password encoder`() {
            userAuthService.registerUser(username, password).block()

            verify(exactly = 1) {
                bcryptPasswordEncoder.encode(password)
            }
        }

        @Test
        fun `should save the user with hashed password in database and return true`() {
            every {
                userAuthRepository.save(any())
            } returns Mono.just(userAuthData)

            val result = userAuthService.registerUser(username, password).block()

            verify(exactly = 1) {
                userAuthRepository.save(userAuthData)
            }
            result shouldBe true
        }
    }
}

package com.yogesh.authapplication.service

import com.yogesh.authapplication.constant.ErrorMessages.userAlreadyExists
import com.yogesh.authapplication.constant.ErrorMessages.userDoesNotExist
import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.repository.UserAuthRepository
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class UserAuthServiceTest {

    // mocked data
    private val username = "username"
    private val password = "password"
    private val hashedPassword = "hashedPassword"
    private val hashedUser = User(username, hashedPassword)
    private val plainUser = User(username, password)
    private val validAuthentication = mockk<Authentication> {
        every { isAuthenticated } returns true
    }

    // mocked dependencies
    private val bcryptPasswordEncoder = mockk<BCryptPasswordEncoder> {
        every { encode(any()) } returns hashedPassword
        every { matches(any(), any()) } returns true
    }
    private val userAuthRepository = mockk<UserAuthRepository>(relaxed = true)
    private val reactiveAuthenticationManager = mockk<ReactiveAuthenticationManager> {
        every { authenticate(any()) } returns validAuthentication.toMono()
    }

    // tests
    private val userAuthService = UserAuthService(
        bcryptPasswordEncoder,
        userAuthRepository,
        reactiveAuthenticationManager
    )

    @Nested
    inner class RegisterUserTests {

        @BeforeEach
        fun setup() {
            every { userAuthRepository.save(any()) } returns hashedUser.toMono()
            every { userAuthRepository.findByUsername(username) } returns Mono.empty()
        }

        @Test
        fun `should hash the plain text password using bcrypt password encoder`() {
            userAuthService.register(plainUser).block()

            verify(exactly = 1) {
                bcryptPasswordEncoder.encode(password)
            }
        }

        @Test
        fun `should save the user with hashed password in database and return true`() {
            val result = userAuthService.register(plainUser).block()

            verify(exactly = 1) {
                userAuthRepository.save(hashedUser)
            }
            result shouldBe true
        }

        @Test
        fun `should not register user if the user already exists`() {
            every {
                userAuthRepository.findByUsername(username)
            } returns hashedUser.toMono()

            val registerUserMono = userAuthService.register(plainUser)

            registerUserMono.test().consumeErrorWith {
                it.message shouldBe userAlreadyExists
            }.verify()
        }
    }

    @Nested
    inner class AuthenticateUserTests {

        @BeforeEach
        fun setup() {
            every { userAuthRepository.findByUsername(username) } returns hashedUser.toMono()
        }

        @Test
        fun `should return error if the user does not exist`() {
            every { userAuthRepository.findByUsername(username) } returns Mono.empty()

            val authenticateMono = userAuthService.authenticate(plainUser)

            authenticateMono.test().consumeErrorWith {
                it.message shouldBe userDoesNotExist
            }.verify()

            verify(exactly = 1) {
                userAuthRepository.findByUsername(plainUser.username)
            }
        }

        @Test
        fun `should verify the password if the user exists`() {
            userAuthService.authenticate(plainUser).block()

            verify(exactly = 1) {
                bcryptPasswordEncoder.matches(plainUser.password, hashedUser.password)
            }
        }

        @Test
        fun `should return true if the user password is valid`() {
            every { bcryptPasswordEncoder.matches(any(), any()) } returns true

            val response = userAuthService.authenticate(plainUser).block()

            response shouldBe true
        }

        @Test
        fun `should return false if the user password is not valid`() {
            every { bcryptPasswordEncoder.matches(any(), any()) } returns false

            val response = userAuthService.authenticate(plainUser).block()

            response shouldBe false
        }
    }

    @Nested
    inner class AuthenticateUserUsingAuthenticationManagerTests {
        @Test
        fun `should authenticate user using authentication manager`() {
            val response = userAuthService.authenticateUsingAuthenticationManager(plainUser).block()

            val authenticationSlot = slot<Authentication>()
            verify(exactly = 1) {
                reactiveAuthenticationManager.authenticate(capture(authenticationSlot))
            }

            authenticationSlot.captured.credentials shouldBe plainUser.password
            authenticationSlot.captured.principal shouldBe plainUser.username
            response shouldBe true
        }
    }
}

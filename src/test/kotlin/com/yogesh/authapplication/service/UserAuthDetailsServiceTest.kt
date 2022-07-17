package com.yogesh.authapplication.service

import com.yogesh.authapplication.constant.ErrorMessages.userDoesNotExist
import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.repository.UserAuthRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

class UserAuthDetailsServiceTest {

    // mocked data
    private val username = "username"
    private val hashedPassword = "hashedPassword"
    private val hashedUser = User(username, hashedPassword)

    // mocked dependencies
    private val userAuthRepository = mockk<UserAuthRepository>(relaxed = true)

    private val userAuthDetailsService = UserAuthDetailsService(userAuthRepository)

    @Test
    fun `should return the user-details given the username if the user exists`() {
        every { userAuthRepository.findByUsername(username) } returns hashedUser.toMono()

        val userDetails = userAuthDetailsService.findByUsername(username).block()

        userDetails!!.username shouldBe username
        userDetails.password shouldBe hashedPassword
        userDetails.authorities shouldNotBe null
    }

    @Test
    fun `should throw error if the user does not exist`() {
        every { userAuthRepository.findByUsername(username) } returns Mono.empty()

        val findUserMono = userAuthDetailsService.findByUsername(username)

        findUserMono.test().consumeErrorWith {
            it.message shouldBe userDoesNotExist
        }.verify()
    }
}

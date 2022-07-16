package com.yogesh.authapplication.integration

import com.yogesh.authapplication.configuration.SecurityTestConfiguration
import com.yogesh.authapplication.constant.ErrorMessages.userAlreadyExists
import com.yogesh.authapplication.model.UserAuthData
import com.yogesh.authapplication.repository.UserAuthRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Import(SecurityTestConfiguration::class)
class UserAuthIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val userAuthRepository: UserAuthRepository
) {
    // mocked data
    private val username = "username"
    private val password = "password"
    private val plainUserAuthData = UserAuthData(username, password)
    private val hashedPassword = "hashedPassword"

    // mocked dependencies
    /*
        Bcrypt adds salt to the plain text automatically resulting into a different hash for the same text
        again and again. Hence, mocking the same.
    */
    @MockBean
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun setup() {
        Mockito.`when`(bCryptPasswordEncoder.encode(password)).thenReturn(hashedPassword)
    }

    @AfterEach
    fun tearDown() {
        userAuthRepository.deleteAll().block()
        Mockito.reset(bCryptPasswordEncoder)
    }

    @Test
    fun `should register user in the system`() {
        webTestClient.post()
            .uri("/v1/auth/user/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUserAuthData))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Boolean>()

        val hashedUserAuthData = userAuthRepository.findByUsername(username).block()
        hashedUserAuthData shouldNotBe null
        hashedUserAuthData!!.username shouldBe username
        hashedUserAuthData.password shouldBe hashedPassword
    }

    @Test
    fun `should not register user in the system if a user with same username already exists`() {
        userAuthRepository.save(plainUserAuthData).block()

        webTestClient.post()
            .uri("/v1/auth/user/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUserAuthData))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody<Exception>()
            .consumeWith {
                it.responseBody!!.message shouldBe userAlreadyExists
            }
    }
}

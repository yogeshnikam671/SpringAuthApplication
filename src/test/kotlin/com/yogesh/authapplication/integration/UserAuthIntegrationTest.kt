package com.yogesh.authapplication.integration

import com.yogesh.authapplication.configuration.SecurityTestConfiguration
import com.yogesh.authapplication.constant.ErrorMessages.userAlreadyExists
import com.yogesh.authapplication.constant.ErrorMessages.userDoesNotExist
import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.model.response.AuthenticationResponse
import com.yogesh.authapplication.repository.UserAuthRepository
import com.yogesh.authapplication.utils.TokenManager
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import org.springframework.security.core.userdetails.User as SecureUser

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
    private val jwtToken = "jwtToken"
    private val userRole = "USER"
    private val plainUser = User(username, password)
    private val hashedPassword = "hashedPassword"
    private val hashedUser = User(username, hashedPassword)
    private val userDetails = SecureUser.withUsername(username).password(hashedPassword).roles(userRole).build()

    // mocked dependencies
    /*
        Bcrypt adds salt to the plain text automatically resulting into a different hash for the same text
        again and again. Hence, mocking the same.
    */
    @MockBean
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    /*
        Since we are adding the expiration time using Date, the token generated will be different at every instance.
        Hence, mocking the same.
    */
    @MockBean
    private lateinit var tokenManager: TokenManager

    @BeforeEach
    fun setup() {
        Mockito.`when`(bCryptPasswordEncoder.encode(password)).thenReturn(hashedPassword)
        Mockito.`when`(bCryptPasswordEncoder.matches(password, hashedPassword)).thenReturn(true)

        Mockito.`when`(tokenManager.generateJwtToken(userDetails)).thenReturn(jwtToken)
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
            .body(BodyInserters.fromValue(plainUser))
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
        userAuthRepository.save(hashedUser).block()

        webTestClient.post()
            .uri("/v1/auth/user/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody<Exception>()
            .consumeWith {
                it.responseBody!!.message shouldBe userAlreadyExists
            }
    }

    @Test
    fun `should authenticate user and return true if the user is valid`() {
        userAuthRepository.save(hashedUser).block()

        webTestClient.post()
            .uri("/v1/auth/user/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Boolean>()
            .isEqualTo(true)
    }

    @Test
    fun `should authenticate user and return false if the user password is not valid`() {
        Mockito.`when`(bCryptPasswordEncoder.matches(password, hashedPassword)).thenReturn(false)
        userAuthRepository.save(hashedUser).block()

        webTestClient.post()
            .uri("/v1/auth/user/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Boolean>()
            .isEqualTo(false)
    }

    @Test
    fun `should throw error if the user to be authenticated does not exist`() {
        webTestClient.post()
            .uri("/v1/auth/user/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody<Exception>()
            .consumeWith {
                it.responseBody!!.message shouldBe userDoesNotExist
            }
    }

    @Nested
    inner class AuthenticationUsingAuthenticationManager {
        @Test
        fun `should authenticate user and return true if valid user`() {
            userAuthRepository.save(hashedUser).block()

            webTestClient.post()
                .uri("/v1/auth/user/spring-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(plainUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful
                .expectBody<AuthenticationResponse>()
                .consumeWith {
                    it.responseBody!!.isAuthenticated shouldBe true
                    it.responseBody!!.token shouldBe jwtToken
                }
        }

        @Test
        fun `should authenticate user and return error if the password is not valid`() {
            Mockito.`when`(bCryptPasswordEncoder.matches(password, hashedPassword)).thenReturn(false)
            userAuthRepository.save(hashedUser).block()

            webTestClient.post()
                .uri("/v1/auth/user/spring-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(plainUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError
                .expectBody<Exception>()
        }

        @Test
        fun `should throw error if the user to be authenticated does not exist`() {
            webTestClient.post()
                .uri("/v1/auth/user/spring-authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(plainUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError
                .expectBody<Exception>()
                .consumeWith {
                    it.responseBody!!.message shouldBe userDoesNotExist
                }
        }
    }
}

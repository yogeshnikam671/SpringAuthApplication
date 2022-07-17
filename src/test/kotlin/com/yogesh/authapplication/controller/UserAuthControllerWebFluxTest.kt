package com.yogesh.authapplication.controller

import com.yogesh.authapplication.configuration.SecurityTestConfiguration
import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.service.UserAuthService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono

/*
Using @WebFluxTest annotation will disable full autoconfiguration and instead apply only configuration relevant to WebFlux tests
(i.e. @Controller, @ControllerAdvice, @JsonComponent, Converter/GenericConverter, and WebFluxConfigurer beans
but not @Component, @Service or @Repository beans).
*/
@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [UserAuthController::class])
@Import(SecurityTestConfiguration::class)
class UserAuthControllerWebFluxTest(
    @Autowired private val webTestClient: WebTestClient
) {
    // mocked data
    private val username = "username"
    private val password = "password"
    private val plainUser = User(username, password)

    // mocked dependencies
    @MockBean
    private lateinit var userAuthService: UserAuthService

    @Test
    fun `should register user`() {
        Mockito.`when`(userAuthService.register(plainUser))
            .thenReturn(Mono.just(true))

        webTestClient.post()
            .uri("/v1/auth/user/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Boolean>()
            .isEqualTo(true)
    }

    @Test
    fun `should authenticate user`() {
        Mockito.`when`(userAuthService.authenticate(plainUser))
            .thenReturn(Mono.just(true))

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
    fun `should authenticate user using authentication manager`() {
        Mockito.`when`(userAuthService.authenticateUsingAuthenticationManager(plainUser))
            .thenReturn(Mono.just(true))

        webTestClient.post()
            .uri("/v1/auth/user/spring-authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(plainUser))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Boolean>()
            .isEqualTo(true)
    }
}

package com.yogesh.authapplication.controller

import com.yogesh.authapplication.model.UserAuthData
import com.yogesh.authapplication.service.UserAuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/auth")
class UserAuthController(
    private val userAuthService: UserAuthService
) {

    @GetMapping("/dummy-unauthorized")
    fun dummyUnAuthorized(): Mono<Any> = Mono.just(mapOf("key" to "value"))

    @GetMapping("/dummy-authorized")
    fun dummyAuthorized(): Mono<Any> = Mono.just(mapOf("key" to "value"))

    @PostMapping("/user/registration")
    fun registerUser(
        @RequestBody userAuthData: UserAuthData
    ): Mono<Boolean> {
        return userAuthService.registerUser(userAuthData.username, userAuthData.password)
    }
}

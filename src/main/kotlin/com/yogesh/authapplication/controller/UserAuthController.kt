package com.yogesh.authapplication.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/auth")
class UserAuthController {

    @GetMapping("/dummy-unauthorized")
    fun dummyUnAuthorized() : Mono<Any> = Mono.just(mapOf("key" to "value"))

    @GetMapping("/dummy-authorized")
    fun dummyAuthorized() : Mono<Any> = Mono.just(mapOf("key" to "value"))
}
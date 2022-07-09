package com.yogesh.authapplication.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController("v1/auth")
class UserAuthController {

    @GetMapping("/dummy")
    fun dummy() = Mono.just(mapOf("key" to "value"))
}
package com.yogesh.authapplication.security

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class JwtAuthenticationManager : ReactiveAuthenticationManager {

    // we do not want to perform any sort of authentication during JWT token validation
    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
       return UsernamePasswordAuthenticationToken("", "", emptyList()).toMono()
    }
}
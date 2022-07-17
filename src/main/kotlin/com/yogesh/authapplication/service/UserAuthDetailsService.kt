package com.yogesh.authapplication.service

import com.yogesh.authapplication.constant.ErrorMessages.userDoesNotExist
import com.yogesh.authapplication.repository.UserAuthRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserAuthDetailsService(
    private val userAuthRepository: UserAuthRepository
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return userAuthRepository.findByUsername(username).map {
            User.withUsername(username)
                .password(it.password)
                .roles(USER)
                .build()
        }.switchIfEmpty {
            Mono.error(Exception(userDoesNotExist))
        }
    }

    companion object {
        private const val USER = "USER"
    }
}

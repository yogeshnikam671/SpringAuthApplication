package com.yogesh.authapplication.service

import com.yogesh.authapplication.model.UserAuthData
import com.yogesh.authapplication.repository.UserAuthRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserAuthService(
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val userAuthRepository: UserAuthRepository
) {

    fun registerUser(username: String, password: String) : Mono<Boolean> {
        val hashedPassword = bcryptPasswordEncoder.encode(password)
        val hashedUserAuthData = UserAuthData(
            username,
            hashedPassword
        )
        return userAuthRepository.save(hashedUserAuthData).map { true }
    }

}
package com.yogesh.authapplication.service

import com.yogesh.authapplication.constant.ErrorMessages.userAlreadyExists
import com.yogesh.authapplication.constant.Messages.userRegisteredSuccessfully
import com.yogesh.authapplication.model.UserAuthData
import com.yogesh.authapplication.repository.UserAuthRepository
import org.apache.logging.log4j.LogManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserAuthService(
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val userAuthRepository: UserAuthRepository
) {
    private val logger = LogManager.getLogger(UserAuthService::class)

    fun registerUser(username: String, password: String): Mono<Boolean> {
        val hashedPassword = bcryptPasswordEncoder.encode(password)
        val hashedUserAuthData = UserAuthData(
            username,
            hashedPassword
        )
        return userAuthRepository.findByUsername(username).flatMap<Boolean?> {
            Mono.error(Exception(userAlreadyExists))
        }.switchIfEmpty(
            userAuthRepository.save(hashedUserAuthData).map { true }
        ).doOnError {
            logger.error(userAlreadyExists)
        }.doOnSuccess {
            logger.info(userRegisteredSuccessfully)
        }
    }
}

package com.yogesh.authapplication.service

import com.yogesh.authapplication.constant.ErrorMessages.invalidPassword
import com.yogesh.authapplication.constant.ErrorMessages.userAlreadyExists
import com.yogesh.authapplication.constant.ErrorMessages.userDoesNotExist
import com.yogesh.authapplication.constant.Messages.userAuthenticatedSuccessfully
import com.yogesh.authapplication.constant.Messages.userRegisteredSuccessfully
import com.yogesh.authapplication.model.User
import com.yogesh.authapplication.model.response.AuthenticationResponse
import com.yogesh.authapplication.repository.UserAuthRepository
import com.yogesh.authapplication.utils.TokenManager
import org.apache.logging.log4j.LogManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserAuthService(
    private val userAuthDetailsService: UserAuthDetailsService,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val userAuthRepository: UserAuthRepository,
    private val reactiveAuthenticationManager: ReactiveAuthenticationManager,
    private val tokenManager: TokenManager
) {
    private val logger = LogManager.getLogger(UserAuthService::class)

    fun register(user: User): Mono<Boolean> {
        val(username, password) = user

        val hashedPassword = bcryptPasswordEncoder.encode(password)
        val hashedUser = User(username, hashedPassword)

        return userAuthRepository.findByUsername(username).flatMap<Boolean?> {
            Mono.error(Exception(userAlreadyExists))
        }.switchIfEmpty(
            userAuthRepository.save(hashedUser).map { true }
        ).doOnError {
            logger.error(userAlreadyExists)
        }.doOnSuccess {
            logger.info(userRegisteredSuccessfully)
        }
    }

    fun authenticate(user: User): Mono<Boolean> {
        val (username, password) = user

        return userAuthRepository.findByUsername(username).map { hashedUserAuthData ->
            bcryptPasswordEncoder.matches(password, hashedUserAuthData.password)
        }.switchIfEmpty {
            Mono.error(Exception(userDoesNotExist))
        }.doOnError {
            logger.info(userDoesNotExist)
        }.doOnSuccess {
            val logMessage = if (it) userAuthenticatedSuccessfully else invalidPassword
            logger.info(logMessage)
        }
    }

    fun authenticateUsingAuthenticationManager(user: User): Mono<AuthenticationResponse> {
        val (username, password) = user
        val token = UsernamePasswordAuthenticationToken(username, password)

        return reactiveAuthenticationManager.authenticate(token)
            .zipWith(
                userAuthDetailsService.findByUsername(username)
            ).map {
                AuthenticationResponse(
                    token = tokenManager.generateJwtToken(it.t2),
                    isAuthenticated = it.t1.isAuthenticated
                )
            }
    }
}

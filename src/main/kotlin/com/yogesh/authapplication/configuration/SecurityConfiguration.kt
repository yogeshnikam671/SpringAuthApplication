package com.yogesh.authapplication.configuration

import com.yogesh.authapplication.service.UserAuthDetailsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@Configuration
class SecurityConfiguration(
    @Value("\${spring.security.bcrypt.password-strength}")
    private val bcryptPasswordStrength: Int,
    private val userAuthDetailsService: UserAuthDetailsService
) {

    @Bean
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/v1/auth/dummy-unauthorized", "/v1/auth/user/*")
            .permitAll()
        return http.build()
    }

    @Bean
    fun bcryptPasswordEncoder() = BCryptPasswordEncoder(bcryptPasswordStrength)

    /*
        We provide two details while creating authManager -
        1. userAuthDetailsService -> which enables the authManager to extract the user from database.
        2. passwordEncoder -> which enables the authManager to automatically encode the plain password
                              and compare with the hashed one.
    */
    @Bean
    fun reactiveAuthenticationManager(): UserDetailsRepositoryReactiveAuthenticationManager {
        val reactiveAuthenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userAuthDetailsService)
        reactiveAuthenticationManager.setPasswordEncoder(bcryptPasswordEncoder())

        return reactiveAuthenticationManager
    }
}

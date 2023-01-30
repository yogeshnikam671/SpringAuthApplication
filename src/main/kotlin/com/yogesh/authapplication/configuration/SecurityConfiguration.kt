package com.yogesh.authapplication.configuration

import com.yogesh.authapplication.configuration.filters.JwtServerAuthenticationConverter
import com.yogesh.authapplication.security.JwtAuthenticationManager
import com.yogesh.authapplication.service.UserAuthDetailsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@EnableWebFluxSecurity
@Configuration
class SecurityConfiguration(
    @Value("\${spring.security.bcrypt.password-strength}")
    private val bcryptPasswordStrength: Int,
    private val userAuthDetailsService: UserAuthDetailsService,
    private val jwtServerAuthenticationConverter: JwtServerAuthenticationConverter
) {

    @Bean
    fun filterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationManager: JwtAuthenticationManager
    ): SecurityWebFilterChain {
        http.authorizeExchange()
            .pathMatchers("/v1/auth/dummy-unauthorized", "/v1/auth/user/*")
            .permitAll()
            .anyExchange()
            .authenticated()
            .and()
            .addFilterAt(
                bearerAuthenticationFilter(jwtAuthenticationManager),
                SecurityWebFiltersOrder.AUTHENTICATION
            )
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
        return http.build()
    }

    @Bean
    fun bcryptPasswordEncoder() = BCryptPasswordEncoder(bcryptPasswordStrength)

    /*
        We provide two details while creating authManager -
        1. userAuthDetailsService -> which enables the authManager to extract the user from database.
        2. passwordEncoder -> which enables the authManager to automatically encode the plain password
                              and compare with the hashed one.
        Note:
        @Primary lets @EnableWebFluxSecurity know that this is the authManager which should be given priority over
        all other authManagers that may be present in the application.
    */
    @Bean
    @Primary
    fun reactiveAuthenticationManager(): UserDetailsRepositoryReactiveAuthenticationManager {
        val reactiveAuthenticationManager = UserDetailsRepositoryReactiveAuthenticationManager(userAuthDetailsService)
        reactiveAuthenticationManager.setPasswordEncoder(bcryptPasswordEncoder())

        return reactiveAuthenticationManager
    }

    fun bearerAuthenticationFilter(
        jwtAuthenticationManager: JwtAuthenticationManager
    ): AuthenticationWebFilter {
        val bearerAuthenticationFilter = AuthenticationWebFilter(jwtAuthenticationManager)
        bearerAuthenticationFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter)
        return bearerAuthenticationFilter
    }
}

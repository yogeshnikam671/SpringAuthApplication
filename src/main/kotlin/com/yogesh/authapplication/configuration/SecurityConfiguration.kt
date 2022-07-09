package com.yogesh.authapplication.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@Configuration
class SecurityConfiguration  {

    @Bean
    fun filterChain(http: ServerHttpSecurity) : SecurityWebFilterChain {
        http.authorizeExchange()
            .pathMatchers("/v1/auth/dummy-unauthorized")
            .permitAll()
        return http.build()
    }
}
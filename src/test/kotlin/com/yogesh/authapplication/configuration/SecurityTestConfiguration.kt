package com.yogesh.authapplication.configuration

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@org.springframework.boot.test.context.TestConfiguration
class SecurityTestConfiguration {

    @Bean
    fun testFilterChain(http: ServerHttpSecurity) : SecurityWebFilterChain {
        http.csrf().disable()
            .authorizeExchange().anyExchange().permitAll()
        return http.build()
    }
}
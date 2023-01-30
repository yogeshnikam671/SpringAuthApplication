package com.yogesh.authapplication.configuration.filters

import com.yogesh.authapplication.service.UserAuthDetailsService
import com.yogesh.authapplication.utils.TokenManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val tokenManager: TokenManager,
    private val userAuthDetailsService: UserAuthDetailsService
): WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return Mono.justOrEmpty(exchange).mapNotNull {
            println("This filter is invoked -->")
            it.request.headers["Authorization"]
        }.filter {
            !it.isNullOrEmpty() && it[0].startsWith("Bearer")
        }.map {
            it[0].replace("Bearer ", "")
        }.zipWhen { jwtToken ->
            val username = tokenManager.extractUsername(jwtToken)
            userAuthDetailsService.findByUsername(username)
        }.filter {
            tokenManager.validateJwtToken(it.t2, it.t1)
        }.doOnNext {
            val authentication = UsernamePasswordAuthenticationToken(
                it.t2.username,
                null,
                it.t2.authorities
            )
            println("setting the authentication object in context -->")
            SecurityContextHolder.getContext().authentication = authentication
        }.then(
            chain.filter(exchange)
        )
    }

}
package com.yogesh.authapplication.configuration.filters

import com.yogesh.authapplication.service.UserAuthDetailsService
import com.yogesh.authapplication.utils.TokenManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtServerAuthenticationConverter(
    private val tokenManager: TokenManager,
    private val userAuthDetailsService: UserAuthDetailsService
) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> {
        return Mono.justOrEmpty(exchange).mapNotNull {
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
        }.map {
            val authentication = UsernamePasswordAuthenticationToken(
                it.t2.username,
                it.t2.password, // Bcrypt encoded password
                it.t2.authorities
            )
            authentication
        }
    }
}

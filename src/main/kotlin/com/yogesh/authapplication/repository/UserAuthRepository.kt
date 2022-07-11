package com.yogesh.authapplication.repository

import com.yogesh.authapplication.model.UserAuthData
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserAuthRepository : ReactiveCrudRepository<UserAuthData, Int> {
    fun findByUsername(username: String) : Mono<UserAuthData>
}
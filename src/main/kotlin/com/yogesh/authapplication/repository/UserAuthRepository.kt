package com.yogesh.authapplication.repository

import com.yogesh.authapplication.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserAuthRepository : ReactiveCrudRepository<User, Int> {
    fun findByUsername(username: String): Mono<User>
}

package com.yogesh.authapplication.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "userAuthData")
data class User(
    val username: String,
    val password: String
)

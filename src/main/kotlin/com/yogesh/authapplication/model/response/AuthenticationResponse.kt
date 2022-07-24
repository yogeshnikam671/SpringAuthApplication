package com.yogesh.authapplication.model.response

data class AuthenticationResponse(
    val token: String,
    val isAuthenticated: Boolean
)

package com.autsing.miga.presentation.data

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val passO: String,
    val userAgent: String,
    val deviceId: String,
    val cookies: Map<String, String>,
)

@Serializable
data class ServiceLoginRespData(
    val code: Int,
    val location: String,
    val description: String,
    val ssecurity: String? = null,
)

@Serializable
data class LoginUrlRespData(
    val loginUrl: String,
    val lp: String,
)

@Serializable
data class LoginLpRespData(
    val location: String,
    val userId: Long,
    val cUserId: String,
    val nonce: Long,
    val ssecurity: String,
    val psecurity: String,
    val passToken: String,
)
